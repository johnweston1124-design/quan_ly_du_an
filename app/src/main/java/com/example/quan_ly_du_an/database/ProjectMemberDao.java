package com.example.quan_ly_du_an.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.quan_ly_du_an.model.MemberWithRole;
import com.example.quan_ly_du_an.model.ProjectMember;

import java.util.List;

@Dao
public interface ProjectMemberDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertProjectMember(ProjectMember projectMember);

    @Query("DELETE FROM project_member_cross_ref WHERE project_id = :projectId AND user_id = :userId")
    int removeMember(long projectId, long userId);

    @Query("SELECT u.id AS userId, u.name, u.email, '' AS avatarUrl, m.role, 0 AS taskCount " +
           "FROM users u " +
           "INNER JOIN project_member_cross_ref m ON u.id = m.user_id " +
           "WHERE m.project_id = :projectId " +
           "ORDER BY CASE m.role WHEN 'ADMIN' THEN 1 WHEN 'LEADER' THEN 2 ELSE 3 END")
    List<MemberWithRole> getMembersByProjectId(long projectId);

    @Query("SELECT EXISTS(SELECT 1 FROM project_member_cross_ref m " +
           "JOIN users u ON m.user_id = u.id " +
           "WHERE m.project_id = :projectId AND u.email = :email)")
    boolean isMemberAlreadyInProject(long projectId, String email);

    @Query("SELECT role FROM project_member_cross_ref WHERE project_id = :projectId AND user_id = :userId")
    String getUserRoleSync(long projectId, long userId);
}
