/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.module.coretix.usermanagement;


import com.module.coretix.commonto.UserActivitiesCountTO;
import com.persist.coretix.modal.usermanagement.UserActivities;
import java.util.List;

/**
 *
 * @author Pragadeesh
 */
public interface IUserActivityService {
   
public List<UserActivities> getUserActivitiesList();
   
 public void addUserActivity(UserActivities userActivity);

 public UserActivitiesCountTO getActivityTypeCounts();
}
