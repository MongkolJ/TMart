package com.example.tmart.bean;

import com.example.tmart.model.SKU;
import com.example.tmart.util.EMUtil;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;


import java.util.List;

@Named("skuBean")
@RequestScoped
public class SKUBean {
    private List<SKU> skuList;


    public List<SKU> getSku(String categoryId) {
        try (EntityManager em = EMUtil.getEMF().createEntityManager()) {

            if (categoryId == null || categoryId.isEmpty()) {
                return em.createQuery("SELECT s FROM SKU s", SKU.class)
                        .getResultList();
            }

            return em.createQuery(
                            "SELECT s FROM SKU s WHERE s.categoryId = :categoryId", SKU.class)
                    .setParameter("categoryId", categoryId)
                    .getResultList();
        }
    }

    public List<SKU> getSkuList() {
        if (skuList == null) {
            try (var em = EMUtil.getEMF().createEntityManager()) {
                skuList = em.createQuery("SELECT s FROM SKU s", SKU.class)
                        .getResultList();
            } catch (Exception e) {
                e.printStackTrace();
                skuList = new java.util.ArrayList<>();
            }
        }
        return skuList;
    }
}
