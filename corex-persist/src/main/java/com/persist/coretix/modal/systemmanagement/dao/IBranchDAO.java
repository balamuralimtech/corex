/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.persist.coretix.modal.systemmanagement.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Branches;
import java.util.List;

/**
 *
 * @author Pragadeesh
 */
public interface IBranchDAO {
    public GeneralConstants addBranch(Branches branch);

    public GeneralConstants updateBranch(Branches branch);

    public GeneralConstants deleteBranch(Branches branch);

    public Branches getBranch(int id);

    public Branches getBranchEntityByBranchName(String branchName);

    public List<Branches> getBranchesList();

    public List<Branches> getBranchesListByOrgId(int orgId);

}
