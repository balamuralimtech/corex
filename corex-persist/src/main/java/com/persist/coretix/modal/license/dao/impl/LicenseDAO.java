/*
 * Copyright (c) 2026 company.name. All rights reserved.
 *
 * This software and its associated documentation are proprietary to company.name.
 * Unauthorized copying, distribution, modification, or use of this software,
 * via any medium, is strictly prohibited without prior written permission.
 *
 * This software is provided "as is", without warranty of any kind, express or implied,
 * including but not limited to the warranties of merchantability, fitness for a
 * particular purpose, and noninfringement. In no event shall the authors or copyright
 * holders be liable for any claim, damages, or other liability arising from the use
 * of this software.
 *
 * Author: Balamurali
 * Project: app.name
 */
package com.persist.coretix.modal.license.dao.impl;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.license.Licenses;
import com.persist.coretix.modal.license.dao.ILicenseDAO;
import com.persist.coretix.modal.systemmanagement.Organizations;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Date;
import java.util.List;

@Named
public class LicenseDAO implements ILicenseDAO {

    @Inject
    private SessionFactory sessionFactory;

    public GeneralConstants addLicense(Licenses license) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(l) from Licenses l where l.organization.id = :organizationId")
                    .setParameter("organizationId", license.getOrganization().getId())
                    .uniqueResult();

            if (count != null && count > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            Organizations organization = (Organizations) session.get(Organizations.class, license.getOrganization().getId());
            license.setOrganization(organization);
            session.save(license);
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public GeneralConstants updateLicense(Licenses license) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();

            Long countById = (Long) session.createQuery(
                            "select count(l) from Licenses l where l.id = :id")
                    .setParameter("id", license.getId())
                    .uniqueResult();
            if (countById == null || countById == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            Long countByOrganization = (Long) session.createQuery(
                            "select count(l) from Licenses l where l.organization.id = :organizationId and l.id != :id")
                    .setParameter("organizationId", license.getOrganization().getId())
                    .setParameter("id", license.getId())
                    .uniqueResult();
            if (countByOrganization != null && countByOrganization > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            Organizations organization = (Organizations) session.get(Organizations.class, license.getOrganization().getId());
            license.setOrganization(organization);
            session.merge(license);
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (ConstraintViolationException e) {
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.ENTRY_IN_USE;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public GeneralConstants deleteLicense(Licenses license) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(l) from Licenses l where l.id = :id")
                    .setParameter("id", license.getId())
                    .uniqueResult();
            if (count == null || count == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            Licenses persistedLicense = (Licenses) session.get(Licenses.class, license.getId());
            session.delete(persistedLicense);
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (ConstraintViolationException e) {
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.ENTRY_IN_USE;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public Licenses getLicense(int id) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();

            @SuppressWarnings("unchecked")
            List<Licenses> list = session.createQuery("from Licenses l where l.id = :id")
                    .setParameter("id", id)
                    .list();

            trans.commit();
            return list.isEmpty() ? null : list.get(0);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public Licenses getLicenseByOrganizationId(int organizationId) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();

            @SuppressWarnings("unchecked")
            List<Licenses> list = session.createQuery(
                            "from Licenses l where l.organization.id = :organizationId")
                    .setParameter("organizationId", organizationId)
                    .list();

            trans.commit();
            return list.isEmpty() ? null : list.get(0);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<Licenses> getLicenseList() {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();

            @SuppressWarnings("unchecked")
            List<Licenses> list = session.createQuery("from Licenses l order by l.id desc").list();

            trans.commit();
            return list;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public boolean isLicenseActiveForOrganization(int organizationId) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();

            Date currentDate = new Date(System.currentTimeMillis());
            Long count = (Long) session.createQuery(
                            "select count(l) from Licenses l where l.organization.id = :organizationId and :currentDate between l.startDate and l.endDate")
                    .setParameter("organizationId", organizationId)
                    .setParameter("currentDate", currentDate)
                    .uniqueResult();

            trans.commit();
            return count != null && count > 0;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}





