package com.mrray.desens.task.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;

/**
 * Created by Arthur on 2017/7/20.
 * 分页查询必须继承该DTO
 */
@ApiModel(description = "分页模型")
public class PageQueryDto {
    /**
     * 页码
     */
    @ApiModelProperty(value = "当前页<br />当前页只能为整数，默认值为1，最小值为1")
    @Min(value = 1, message = "PAGE_LT_ONE")
    private int page = 1;

    /**
     * 分页大小
     */
    @ApiModelProperty(value = "分页大小<br />分页大小只能为整数，默认值为10，最小值为3，最大值为30")
    @Range(min = 3, max = 30, message = "SIZE_NOTIN_RANGE")
    private int size = 10;

    /**
     * 排序属性
     */
    @ApiModelProperty(value = "排序属性<br />排序属性是根据模型的属性来的，默认值为主键id", allowEmptyValue = true)
    private String property = "id";

    /**
     * 排序方向
     */
    @ApiModelProperty(value = "排序方向<br />排序方向有倒叙和顺序，ASC标书升序，DESC表示降序", allowableValues = "ASC, DESC", allowEmptyValue = true)
    private String direction = "DESC";

    public PageQueryDto() {

    }

    public PageQueryDto(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public PageQueryDto(int page, int size, String property, String direction) {
        this.page = page;
        this.size = size;
        this.property = property;
        this.direction = direction;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        property = StringUtils.isNotBlank(property) ? property : "id";
        this.property = property;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = "ASC".equals(direction.toUpperCase()) ? "ASC" : "DESC";
    }
}
