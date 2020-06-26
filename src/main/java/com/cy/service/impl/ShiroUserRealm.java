package com.cy.service.impl;

import com.cy.entity.User;
import com.cy.mapper.MenuMapper;
import com.cy.mapper.RoleMenuMapper;
import com.cy.mapper.UserMapper;
import com.cy.mapper.UserRoleMapper;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author wh
 * @date 2020/6/21
 * Description:realm
 */
@Service
public class ShiroUserRealm extends AuthorizingRealm {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper sysUserRoleDao;

    @Autowired
    private RoleMenuMapper sysRoleMenuDao;

    @Autowired
    private MenuMapper sysMenuDao;


    /**
     * 授权
     *
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        //1.获取登录用户信息，例如用户id
        User user = (User) principalCollection.getPrimaryPrincipal();
        Integer userId = user.getId();
        //2.基于用户id获取用户拥有的角色(sys_user_roles)
        List<Integer> roleIds = sysUserRoleDao.findRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.size() == 0) {
            throw new AuthorizationException();
        }
        //3.基于角色id获取菜单id(sys_role_menus)
        Integer[] array = {};
        List<Integer> menuIds = sysRoleMenuDao.findMenuIdsByRoleIds(roleIds.toArray(array));
        if (menuIds == null || menuIds.size() == 0) {
            throw new AuthorizationException();
        }
        //4.基于菜单id获取权限标识(sys_menus)
        List<String> permissions = sysMenuDao.findPermissions(menuIds.toArray(array));
        //5.对权限标识信息进行封装并返回
        Set<String> set = new HashSet<>();
        for (String per : permissions) {
            if (!StringUtils.isEmpty(per)) {
                set.add(per);
            }
        }
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setStringPermissions(set);
        //返回给授权管理器
        return info;
    }

    /**
     * 设置凭证匹配器(与用户添加操作使用相同的加密算法)
     */
    @Override
    public void setCredentialsMatcher(CredentialsMatcher credentialsMatcher) {
        //构建凭证匹配对象
        HashedCredentialsMatcher cMatcher = new HashedCredentialsMatcher();
        //设置加密算法
        cMatcher.setHashAlgorithmName("MD5");
        //设置加密次数
        cMatcher.setHashIterations(1);
        super.setCredentialsMatcher(cMatcher);
    }

    /**
     * 认证
     *
     * @param authenticationToken 认证令牌
     * @return
     * @throws AuthenticationException 认证异常
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //1.获取用户名(用户页面输入)
        UsernamePasswordToken userTokenn = (UsernamePasswordToken) authenticationToken;
        String username = userTokenn.getUsername();
        //2.基于用户名查询用户信息
        User user = userMapper.findUserByUserName(username);
        //3.判定用户是否存在
        if (user == null) {
            throw new UnknownAccountException();
        }
        //4.判定用户是否已被禁用。
        if (user.getValid() == 0) {
            throw new LockedAccountException();
        }
        //5.封装用户信息
        ByteSource credentialsSalt = ByteSource.Util.bytes(user.getSalt());
        //记住：构建什么对象要看方法的返回值
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                user,//principal (身份)
                user.getPassword(),//hashedCredentials
                credentialsSalt, //credentialsSalt
                getName()
        );
        //6.返回封装结果
        //返回值会传递给认证管理器(后续
        //认证管理器会通过此信息完成认证操作)
        return authenticationInfo;
    }
}
