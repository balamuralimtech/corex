/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.persist.coretix.modal.usermanagement.dao;
import com.persist.coretix.modal.usermanagement.UserActivities;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Pragadeesh
 */
public interface IUserActivityDAO {
    
    public void addUserActivity(UserActivities userActivity);

    public List<UserActivities> getUserActivitiesList();

    public Map<String, Integer> getActivityTypeCounts();
   
}
