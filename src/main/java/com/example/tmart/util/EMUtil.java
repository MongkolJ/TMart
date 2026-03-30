package com.example.tmart.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class EMUtil {

    private static EntityManagerFactory emf;

    public static synchronized EntityManagerFactory getEMF() {
        if (emf == null || !emf.isOpen()) {
            try {
                Thread.currentThread().setContextClassLoader(
                        EMUtil.class.getClassLoader()
                );

                emf = Persistence.createEntityManagerFactory("TMartPU");

            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
        return emf;
    }
}