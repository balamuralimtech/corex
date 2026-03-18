/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.module.coretix.systemmanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Branches;
import java.util.List;

/**
 *
 * @author Pragadeesh
 */
public interface IBranchService {

    public GeneralConstants addBranch(UserActivityTO userActivityTO,Branches branch);

    public GeneralConstants updateBranch(UserActivityTO userActivityTO,Branches branch);

    public GeneralConstants deleteBranch(UserActivityTO userActivityTO,Branches branch);

    public Branches getBranchById(int id);

    public Branches getBranchEntityByBranchName(String branchName);

    public List<Branches> getBranchesList();

    public List<Branches> getBranchesListByOrgId(int orgId);

}
