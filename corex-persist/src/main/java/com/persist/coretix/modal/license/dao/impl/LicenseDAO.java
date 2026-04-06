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
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@Named
public class LicenseDAO implements ILicenseDAO {

    private static final Logger logger = LoggerFactory.getLogger(LicenseDAO.class);

    @Inject
    private SessionFactory sessionFactory;

    public GeneralConstants addLicense(Licenses license) {
        Session session = sessionFactory.getCurrentSession();
        try {
            Long count = (Long) session.createQuery(
                            "select count(l) from Licenses l where l.organization.id = :organizationId")
                    .setParameter("organizationId", license.getOrganization().getId())
                    .uniqueResult();

            if (count != null && count > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            Organizations organization = (Organizations) session.get(Organizations.class, license.getOrganization().getId());
            license.setOrganization(organization);
            Timestamp now = new Timestamp(System.currentTimeMillis());
            license.setCreatedAt(now);
            license.setUpdatedAt(now);
            session.save(license);
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception e) {
            logger.error("Unable to add license for organization {}", license.getOrganization() == null ? null : license.getOrganization().getId(), e);
            session.clear();
            return GeneralConstants.FAILED;
        }
    }

    public GeneralConstants updateLicense(Licenses license) {
        Session session = sessionFactory.getCurrentSession();
        try {
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
            Licenses persistedLicense = (Licenses) session.get(Licenses.class, license.getId());
            if (persistedLicense != null) {
                license.setCreatedAt(persistedLicense.getCreatedAt());
            }
            license.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            license.setOrganization(organization);
            session.merge(license);
            return GeneralConstants.SUCCESSFUL;
        } catch (ConstraintViolationException e) {
            logger.warn("License update failed because the entry is in use. License id={}", license.getId(), e);
            session.clear();
            return GeneralConstants.ENTRY_IN_USE;
        } catch (Exception e) {
            logger.error("Unable to update license {}", license.getId(), e);
            session.clear();
            return GeneralConstants.FAILED;
        }
    }

    public GeneralConstants deleteLicense(Licenses license) {
        Session session = sessionFactory.getCurrentSession();
        try {
            Long count = (Long) session.createQuery(
                            "select count(l) from Licenses l where l.id = :id")
                    .setParameter("id", license.getId())
                    .uniqueResult();
            if (count == null || count == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            Licenses persistedLicense = (Licenses) session.get(Licenses.class, license.getId());
            session.delete(persistedLicense);
            return GeneralConstants.SUCCESSFUL;
        } catch (ConstraintViolationException e) {
            logger.warn("License delete failed because the entry is in use. License id={}", license.getId(), e);
            session.clear();
            return GeneralConstants.ENTRY_IN_USE;
        } catch (Exception e) {
            logger.error("Unable to delete license {}", license.getId(), e);
            session.clear();
            return GeneralConstants.FAILED;
        }
    }

    public Licenses getLicense(int id) {
        Session session = sessionFactory.getCurrentSession();
        @SuppressWarnings("unchecked")
        List<Licenses> list = session.createQuery("from Licenses l where l.id = :id")
                .setParameter("id", id)
                .list();
        return list.isEmpty() ? null : list.get(0);
    }

    public Licenses getLicenseByOrganizationId(int organizationId) {
        Session session = sessionFactory.getCurrentSession();
        @SuppressWarnings("unchecked")
        List<Licenses> list = session.createQuery(
                        "from Licenses l where l.organization.id = :organizationId")
                .setParameter("organizationId", organizationId)
                .list();
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Licenses> getLicenseList() {
        Session session = sessionFactory.getCurrentSession();
        @SuppressWarnings("unchecked")
        List<Licenses> list = session.createQuery("from Licenses l order by l.id desc").list();
        return list;
    }

    public boolean isLicenseActiveForOrganization(int organizationId) {
        Session session = sessionFactory.getCurrentSession();
        Date currentDate = new Date(System.currentTimeMillis());
        Long count = (Long) session.createQuery(
                        "select count(l) from Licenses l where l.organization.id = :organizationId and :currentDate between l.startDate and l.endDate")
                .setParameter("organizationId", organizationId)
                .setParameter("currentDate", currentDate)
                .uniqueResult();
        return count != null && count > 0;
    }
}





