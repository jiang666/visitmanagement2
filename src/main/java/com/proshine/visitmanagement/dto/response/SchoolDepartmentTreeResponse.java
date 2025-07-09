package com.proshine.visitmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 学校-院系树节点响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolDepartmentTreeResponse {
    private Long id;
    private String name;
    private List<DepartmentNode> departments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentNode {
        private Long id;
        private String name;
    }
}
