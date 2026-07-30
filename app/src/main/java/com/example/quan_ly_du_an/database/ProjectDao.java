package com.example.quan_ly_du_an.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quan_ly_du_an.model.Project;
import com.example.quan_ly_du_an.model.ProjectWithRole;

import java.util.List;

@Dao
public interface ProjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertProject(Project project);

    @Update
    int updateProject(Project project);

    @Query("DELETE FROM projects WHERE project_id = :projectId")
    int deleteProject(long projectId);

    @Query("SELECT p.project_id AS projectId, p.title, p.description, p.status, m.role " +
           "FROM projects p " +
           "INNER JOIN project_member_cross_ref m ON p.project_id = m.project_id " +
           "WHERE m.user_id = :userId " +
           "ORDER BY p.project_id DESC")
    List<ProjectWithRole> getProjectsForUser(long userId);

    @Query("SELECT p.project_id AS projectId, p.title, p.description, p.status, m.role " +
           "FROM projects p " +
           "INNER JOIN project_member_cross_ref m ON p.project_id = m.project_id " +
           "WHERE m.user_id = :userId AND (p.title LIKE :keyword OR p.description LIKE :keyword)")
    List<ProjectWithRole> searchProjects(long userId, String keyword);

    @Query("SELECT p.project_id AS projectId, p.title, p.description, p.status, m.role " +
           "FROM projects p " +
           "INNER JOIN project_member_cross_ref m ON p.project_id = m.project_id " +
           "WHERE m.user_id = :userId " +
           "ORDER BY p.project_id DESC LIMIT :limit")
    androidx.lifecycle.LiveData<List<ProjectWithRole>> getLatestProjectsForUser(long userId, int limit);
}
