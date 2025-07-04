package com.proshine.visitmanagement.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 学校请求类
 *
 * @author System
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolRequest {

    /**
     * 学校名称
     */
    @NotBlank(message = "学校名称不能为空")
    @Size(min = 2, max = 200, message = "学校名称长度为2-200个字符")
    private String name;

    /**
     * 学校地址
     */
    @Size(max = 500, message = "学校地址长度不能超过500个字符")
    private String address;

    /**
     * 省份
     */
    @NotBlank(message = "省份不能为空")
    @Size(max = 50, message = "省份长度不能超过50个字符")
    private String province;

    /**
     * 城市
     */
    @NotBlank(message = "城市不能为空")
    @Size(max = 50, message = "城市长度不能超过50个字符")
    private String city;

    /**
     * 学校类型
     */
    @Pattern(regexp = "^(_985|_211|DOUBLE_FIRST_CLASS|REGULAR)$",
            message = "学校类型必须是: _985, _211, DOUBLE_FIRST_CLASS, REGULAR 之一")
    private String schoolType;

    /**
     * 联系电话
     */
    @Pattern(regexp = "^[0-9\\-\\+\\(\\)\\s]{0,20}$",
            message = "联系电话格式不正确")
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String contactPhone;

    /**
     * 学校网站
     */
    @Pattern(regexp = "^$|^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
            message = "网站URL格式不正确")
    @Size(max = 200, message = "网站URL长度不能超过200个字符")
    private String website;
}