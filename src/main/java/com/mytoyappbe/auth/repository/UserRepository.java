/**
 * @file UserRepository.java
 * @description {@link User} 엔티티에 대한 데이터베이스 접근을 처리하는 Spring Data JPA 리포지토리 인터페이스입니다.
 *              사용자 정보를 조회하고 관리하는 기능을 제공합니다.
 */

package com.mytoyappbe.auth.repository;

import com.mytoyappbe.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @interface UserRepository
 * @description {@link User} 엔티티에 대한 데이터베이스 접근을 처리하는 Spring Data JPA 리포지토리입니다.
 *              `JpaRepository`를 상속받아 기본적인 CRUD(Create, Read, Update, Delete) 연산 및
 *              사용자 이름으로 조회하는 기능을 자동으로 제공합니다.
 *              제네릭 타입으로 엔티티 클래스({@link User})와 ID 타입({@link Long})을 지정합니다.
 */
@Repository // Spring 리포지토리 컴포넌트임을 나타냅니다.
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * @method findByUsername
     * @description 주어진 사용자 이름(username)을 기반으로 {@link User} 엔티티를 조회합니다.
     *              Spring Data JPA의 쿼리 메서드 기능을 활용하여 메서드 이름만으로 쿼리를 자동으로 생성합니다.
     * @param {String} username - 조회할 사용자의 이름
     * @returns {Optional<User>} 해당 사용자 이름으로 찾은 {@link User} 객체를 포함하는 {@link Optional}, 없으면 빈 {@link Optional}
     */
    Optional<User> findByUsername(String username);
}