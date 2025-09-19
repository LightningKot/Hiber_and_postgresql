package com.example.dao;

import com.example.Main;
import com.example.entity.User;
import com.example.util.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    public Optional<Long> createUser(User user) {
        logger.debug("createUser DAO start...");
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        Long userId = null;

        try {
            transaction = session.beginTransaction();
            userId = (Long) session.save(user);
            transaction.commit();
            return Optional.of(userId);

        } catch (ConstraintViolationException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error(RED+"Constraint violation: {}" + RESET);
            return Optional.empty();

        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error(RED+"Hibernate error: {}", e.getMessage()+RESET);
            return Optional.empty();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error(RED + "Unexpected error: {}", e.getMessage() + RESET);
            return Optional.empty();

        } finally {
            session.close();
        }
    }

    public Optional<User> getUserById(Long id) {
        logger.debug("getUserById start...");
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            User user = session.get(User.class, id);
            return Optional.ofNullable(user);
        } catch (HibernateException e) {
            logger.error(RED + "Hibernate error: {}", e.getMessage()+RESET);
            return Optional.empty();
        } finally {
            session.close();
        }
    }

    public List<User> getAllUsers() {
        logger.debug("getAllUsers start...");
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Query<User> query = session.createQuery("FROM User", User.class);
            return query.list();
        } catch (HibernateException e) {
            logger.error(RED + "Hibernate error: {}", e.getMessage() + RESET);
            return List.of();
        } finally {
            session.close();
        }
    }

    public boolean updateUser(Long id, String name, String email, Integer age) {
        logger.debug("updateUser start...");
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            // Создаем HQL запрос для обновления
            StringBuilder hql = new StringBuilder("UPDATE User SET ");
            boolean hasUpdates = false;

            if (name != null && !name.isEmpty()) {
                hql.append("name = :name");
                hasUpdates = true;
            }

            if (email != null && !email.isEmpty()) {
                if (hasUpdates) hql.append(", ");
                hql.append("email = :email");
                hasUpdates = true;
            }

            if (age != null) {
                if (hasUpdates) hql.append(", ");
                hql.append("age = :age");
                hasUpdates = true;
            }

            if (!hasUpdates) {
                System.out.println("No fields to update");
                transaction.rollback();
                return false;
            }

            hql.append(" WHERE id = :id");

            Query<?> query = session.createQuery(hql.toString());

            if (name != null && !name.isEmpty()) {
                query.setParameter("name", name);
            }

            if (email != null && !email.isEmpty()) {
                query.setParameter("email", email);
            }

            if (age != null) {
                query.setParameter("age", age);
            }

            query.setParameter("id", id);

            int updatedCount = query.executeUpdate();
            transaction.commit();

            return updatedCount > 0;

        } catch (ConstraintViolationException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error(RED + "Constraint violation: {}", e.getMessage() + RESET);
            return false;

        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error(RED + "Hibernate error: {}", e.getMessage() + RESET);
            return false;

        } finally {
            session.close();
        }
    }

    public boolean deleteUser(Long id) {
        logger.debug("deleteUser start...");
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            Query<?> query = session.createQuery("DELETE FROM User WHERE id = :id");
            query.setParameter("id", id);

            int deletedCount = query.executeUpdate();
            transaction.commit();

            return deletedCount > 0;

        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error(RED + "Hibernate error: {}", e.getMessage() + RESET);
            return false;

        } finally {
            session.close();
        }
    }

    public Optional<User> getUserByEmail(String email) {
        logger.debug("getUserByEmail start...");
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Query<User> query = session.createQuery(
                    "FROM User WHERE email = :email", User.class);
            query.setParameter("email", email);
            return Optional.ofNullable(query.uniqueResult());
        } catch (HibernateException e) {
            logger.error(RED + "Hibernate error: {}", e.getMessage() + RESET);
            return Optional.empty();
        } finally {
            session.close();
        }
    }

    // ANSI escape codes
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\033[1;91m";
}