package com.portfolio.chat.repository;

import com.portfolio.chat.entity.ChatRoom;
import com.portfolio.chat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT cr FROM ChatRoom cr JOIN cr.members m WHERE m = :user")
    List<ChatRoom> findAllByMember(@Param("user") User user);

    @Query("SELECT cr FROM ChatRoom cr LEFT JOIN FETCH cr.members WHERE cr.id = :id")
    Optional<ChatRoom> findByIdWithMembers(@Param("id") Long id);
}
