package com.reverse.hr.internal.persistence;

import com.reverse.hr.internal.persistence.row.OrgMemberRow;
import com.reverse.hr.internal.persistence.row.OrgTreeNodeRow;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrganizationMapper {

    List<OrgTreeNodeRow> findOrganizationTree();

    List<OrgMemberRow> findOrganizationMembers(@Param("orgId") Long orgId);

    Long findMyOrgIdByEmployeeId(@Param("employeeId") Long employeeId);

    List<OrgMemberRow> findMembersInSubtree(
            @Param("ancestorOrgId") Long ancestorOrgId, @Param("filterOrgId") Long filterOrgId);

    int existsSubtreeAccess(
            @Param("ancestorOrgId") Long ancestorOrgId,
            @Param("descendantOrgId") Long descendantOrgId);
}
