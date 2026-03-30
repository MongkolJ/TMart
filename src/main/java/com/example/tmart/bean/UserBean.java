package com.example.tmart.bean;

import com.example.tmart.model.User;
import com.example.tmart.util.EMUtil;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;

import java.io.Serializable;
import java.util.UUID;

@Named
@SessionScoped
public class UserBean implements Serializable {

    private User user;
    private String username;
    private String password;
    private String name;
    private String confirmPassword;

    @Inject
    private CartBean cartBean;

    public String login() {
        try (EntityManager em = EMUtil.getEMF().createEntityManager()) {
            user = em.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username " +
                                    "AND u.password = :password", User.class)
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getSingleResult();

            return "index?faces-redirect=true";

        } catch (NoResultException e) {
            return null;
        }
    }

    public String register() {
        if (!password.equals(confirmPassword)) {
            return null;
        }

        User newUser = new User();
        newUser.setId(UUID.randomUUID().toString());
        newUser.setName(name);
        newUser.setUsername(username);
        newUser.setPassword(password);

        EntityManager em = EMUtil.getEMF().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(newUser);
            em.getTransaction().commit();
            return "login?faces-redirect=true";
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public CartBean getCartBean() {
        return cartBean;
    }

    public void setCartBean(CartBean cartBean) {
        this.cartBean = cartBean;
    }
}
