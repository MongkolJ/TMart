package com.example.tmart.bean;

import com.example.tmart.model.Cart;
import com.example.tmart.model.ItemCart;
import com.example.tmart.model.Order;
import com.example.tmart.model.SKU;
import com.example.tmart.util.EMUtil;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Named("cartBean")
@SessionScoped
public class CartBean implements Serializable {
    private Cart cart;
    private List<ItemCart> itemCartList;

    @Inject
    private UserBean userBean;

    @Inject
    private SKUBean skuBean;

    public Cart getCart() {
        if (userBean.getUser() == null) return null;
        if (cart != null) return cart;

        String userId = userBean.getUser().getId();
        try (EntityManager em = EMUtil.getEMF().createEntityManager()) {
            List<Cart> carts = em.createQuery(
                            "SELECT c FROM Cart c WHERE c.customerId = :customerId AND c.isActive = true",
                            Cart.class)
                    .setParameter("customerId", userId)
                    .getResultList();          // ← not getSingleResult()

            if (carts.isEmpty()) {
                cart = new Cart();
                cart.setId(UUID.randomUUID().toString());
                cart.setCustomerId(userId);
                cart.setActive(true);
                cart.setTotalPrice(0);

                em.getTransaction().begin();
                em.persist(cart);
                em.getTransaction().commit();
            } else {
                cart = carts.get(0);
            }

            itemCartList = getItemCartList();
            return cart;

        }
    }

    public void addItem(SKU sku) {
        EntityManager em = EMUtil.getEMF().createEntityManager();
        try {
            em.getTransaction().begin();

            List<ItemCart> existing = em.createQuery(
                            "SELECT i FROM ItemCart i WHERE i.cartId = :cartId AND i.skuId = :skuId",
                            ItemCart.class)
                    .setParameter("cartId", cart.getId())
                    .setParameter("skuId", sku.getId())
                    .getResultList();

            if (!existing.isEmpty()) {
                ItemCart item = existing.get(0);
                item.setQuantity(item.getQuantity() + 1);
                item.setTotalPrice(item.getQuantity() * sku.getPrice());
                em.merge(item);
            } else {
                ItemCart item = new ItemCart();
                item.setId(UUID.randomUUID().toString());
                item.setCartId(cart.getId());
                item.setSkuId(sku.getId());
                item.setQuantity(1);
                item.setTotalPrice(sku.getPrice());
                em.persist(item);
            }

            // Recalculate cart total
            List<ItemCart> allItems = em.createQuery(
                            "SELECT i FROM ItemCart i WHERE i.cartId = :cartId",
                            ItemCart.class)
                    .setParameter("cartId", cart.getId())
                    .getResultList();

            double total = allItems.stream()
                    .mapToDouble(ItemCart::getTotalPrice)
                    .sum();

            Cart managedCart = em.find(Cart.class, cart.getId());
            managedCart.setTotalPrice(total);
            em.merge(managedCart);
            cart.setTotalPrice(total);

            em.getTransaction().commit();

            itemCartList = allItems;

        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public List<ItemCart> getItemCartList() {
        try (EntityManager em = EMUtil.getEMF().createEntityManager()) {
            this.itemCartList = em.createQuery("SELECT items FROM ItemCart items WHERE items.cartId = :cartId", ItemCart.class)
                    .setParameter("cartId", this.cart.getId())
                    .getResultList();
            return this.itemCartList;
        }
    }

    public String checkout() {
        EntityManager em = EMUtil.getEMF().createEntityManager();
        try {
            em.getTransaction().begin();

            // Create order
            Order order = new Order();
            order.setId(UUID.randomUUID().toString());
            order.setCartId(cart.getId());
            order.setCustomerId(cart.getCustomerId());
            order.setTotalPrice(cart.getTotalPrice());
            em.persist(order);

            // Deactivate current cart
            Cart managedCart = em.find(Cart.class, cart.getId());
            if (managedCart != null) {
                managedCart.setActive(false);
                em.merge(managedCart);
            }

            // Create new empty cart
            Cart newCart = new Cart();
            newCart.setId(UUID.randomUUID().toString());
            newCart.setCustomerId(userBean.getUser().getId());
            newCart.setTotalPrice(0);
            newCart.setActive(true);
            em.persist(newCart);

            em.getTransaction().commit();

            // Update session state
            cart = newCart;
            itemCartList = new java.util.ArrayList<>();

            return "index?faces-redirect=true";

        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    public int getCartCount() {
        if (userBean.getUser() == null) return 0;
        if (cart == null) {
            getCart();
        }
        return itemCartList == null ? 0 : itemCartList.size();
    }

    public void removeItem(ItemCart item) {
        EntityManager em = EMUtil.getEMF().createEntityManager();
        try {
            em.getTransaction().begin();

            ItemCart managed = em.find(ItemCart.class, item.getId());
            if (managed != null) {
                em.remove(managed);
            }

            List<ItemCart> allItems = em.createQuery(
                            "SELECT i FROM ItemCart i WHERE i.cartId = :cartId",
                            ItemCart.class)
                    .setParameter("cartId", cart.getId())
                    .getResultList();

            double total = allItems.stream()
                    .mapToDouble(ItemCart::getTotalPrice)
                    .sum();

            Cart managedCart = em.find(Cart.class, cart.getId());
            managedCart.setTotalPrice(total);
            em.merge(managedCart);
            cart.setTotalPrice(total);

            em.getTransaction().commit();

            itemCartList = allItems;

        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void clearCart() {
        EntityManager em = EMUtil.getEMF().createEntityManager();
        try {
            em.getTransaction().begin();

            em.createQuery("DELETE FROM ItemCart i WHERE i.cartId = :cartId")
                    .setParameter("cartId", cart.getId())
                    .executeUpdate();

            Cart managedCart = em.find(Cart.class, cart.getId());
            managedCart.setTotalPrice(0);
            em.merge(managedCart);
            cart.setTotalPrice(0);

            em.getTransaction().commit();

            itemCartList = new java.util.ArrayList<>();

        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
