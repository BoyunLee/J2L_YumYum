package com.ssafy.yumyum.domain.user.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.yumyum.domain.user.entity.User;
import com.ssafy.yumyum.global.security.oauth2.user.OAuth2Provider;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserDao {

    List<User> findAll();

    Optional<User> findById(@Param("id") Long id);

    Optional<User> findByProviderAndProviderUserId(OAuth2Provider provider, String providerUserId);

    void insert(User user);

    boolean update(User user);

    boolean deactivate(@Param("id") Long id);

    void delete(User user);
}


