package com.example.tmart.bean;
import com.example.tmart.model.User;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;

import java.io.Serializable;

@Named
@SessionScoped
public class UserBean  implements Serializable {
    @PersistenceContext
    private EntityManager em;

    private User user;

    public User login(String username, String password) {
        try {
            User user = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username" +
                    "AND u.password = :password", User.class)
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getSingleResult();
            this.user = user;
            return user;

        } catch (NoResultException e) {
            return null;
        }
    }
}
