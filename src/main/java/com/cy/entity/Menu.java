package com.cy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wh
 * @date 2020/5/27
 * Description: 菜单实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Menu implements Serializable {

    private static final long serialVersionUID = 63882295606154655L;

    /**
     * 菜单id
     */
    private Integer id;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 菜单url: log/doFindPageObjects
     */
    private String url;

    /**
     * 菜单类型(两种:按钮,普通菜单)
     */
    private Integer type = 1;

    /**
     * 排序(序号)
     */
    private Integer sort;

    /**
     * 备注
     */
    private String note;

    /**
     * 上级菜单id
     */
    private Integer parentId;

    /**
     * 菜单对应的权限标识(sys:log:delete)
     */
    private String permission;

    /**
     * 创建用户
     */
    private String createdUser;

    /**
     * 修改用户
     */
    private String modifiedUser;

    private Date createdTime;
    private Date modifiedTime;
}
