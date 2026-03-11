package com.reverse.performance.internal.application;

import com.reverse.hr.HrFacade;
import com.reverse.hr.dto.EmployeeProfileDTO;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PerformanceHrMemberResolver {

    private final HrFacade hrFacade;

    public List<OrganizationMemberSnapshot> getMyOrganizationMembers(Long employeeId) {
        try {
            Method method = hrFacade.getClass().getMethod("getMyOrganizationMembers", Long.class);
            Object result = method.invoke(hrFacade, employeeId);
            if (!(result instanceof Iterable<?> iterable)) {
                throw new IllegalStateException(
                        "HR facade must return an iterable organization member list.");
            }

            List<OrganizationMemberSnapshot> members = new ArrayList<>();
            for (Object item : iterable) {
                if (item == null) {
                    continue;
                }
                members.add(
                        new OrganizationMemberSnapshot(
                                readLong(item, "employeeId"),
                                readString(item, "employeeName"),
                                readLong(item, "orgId"),
                                readString(item, "orgName"),
                                readLong(item, "positionId"),
                                readString(item, "positionName"),
                                readString(item, "rankName"),
                                readString(item, "jobName")));
            }
            return members;
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(
                    "HrFacade.getMyOrganizationMembers(Long) is required for performance team/member features.",
                    ex);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalStateException(
                    "Failed to load organization members from HR facade.", ex);
        }
    }

    public EmployeeProfileSnapshot getEmployeeProfile(Long employeeId) {
        EmployeeProfileDTO profile = hrFacade.getEmployeeProfile(employeeId);
        return new EmployeeProfileSnapshot(
                profile.employeeId(),
                profile.employeeName(),
                profile.email(),
                profile.orgName(),
                profile.rankName(),
                profile.positionName(),
                profile.jobName());
    }

    public Map<Long, EmployeeProfileSnapshot> getEmployeeProfiles(List<Long> employeeIds) {
        return employeeIds.stream()
                .distinct()
                .collect(Collectors.toMap(Function.identity(), this::getEmployeeProfile));
    }

    public Long resolveOrgId(Long employeeId) {
        return getMyOrganizationMembers(employeeId).stream()
                .filter(member -> employeeId.equals(member.employeeId()))
                .map(OrganizationMemberSnapshot::orgId)
                .findFirst()
                .orElseGet(
                        () ->
                                getMyOrganizationMembers(employeeId).stream()
                                        .map(OrganizationMemberSnapshot::orgId)
                                        .filter(id -> id != null)
                                        .findFirst()
                                        .orElse(null));
    }

    private Long readLong(Object target, String methodName) {
        Object value = invokeAccessor(target, methodName);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalStateException("Expected numeric value from accessor: " + methodName);
    }

    private String readString(Object target, String methodName) {
        Object value = invokeAccessor(target, methodName);
        return value == null ? null : String.valueOf(value);
    }

    private Object invokeAccessor(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(
                    "Missing HR organization member accessor: " + methodName, ex);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalStateException(
                    "Failed to read HR organization member accessor: " + methodName, ex);
        }
    }

    public record OrganizationMemberSnapshot(
            Long employeeId,
            String employeeName,
            Long orgId,
            String orgName,
            Long positionId,
            String positionName,
            String rankName,
            String jobName) {}

    public record EmployeeProfileSnapshot(
            Long employeeId,
            String employeeName,
            String email,
            String orgName,
            String rankName,
            String positionName,
            String jobName) {}
}
