package com.reverse.hr.internal.persistence;

import com.reverse.hr.internal.persistence.row.OrgMemberRow;
import com.reverse.hr.internal.persistence.row.OrgTreeNodeRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrganizationMapper {

    List<OrgTreeNodeRow> findOrganizationTree();

    List<OrgMemberRow> findOrganizationMembers(@Param("orgId") Long orgId);

    Long findMyOrgIdByEmployeeId(@Param("employeeId") Long employeeId);
}
