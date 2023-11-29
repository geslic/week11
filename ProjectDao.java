package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;


@SuppressWarnings("unused")
public class ProjectDao extends DaoBase { 
	
	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";
	/**
	 * 
	 * @param project The project object to insert.
	 * @return The Project object with the primary key.
	 * @throws DbException Thrown if any error occurs inserting the row.
	 */
	
	
	public Project insertProject(Project project) {
		// TODO Auto-generated method stub
		
		
		// @formatter:off
		String sql = ""
			+ "INSERT INTO " + PROJECT_TABLE + " "
			+ "(project_name, estimated_hours, actual_hours, difficulty, notes) "
			+ "VALUES "
			+ "(?, ?, ?, ?, ?)";
		// @formatter:on
		
		try(Connection conn = DbConnection.getConnection()) {
	
			startTransaction(conn); // Transaction started
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				
				stmt.executeUpdate();
				
				Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
				commitTransaction(conn);
				
				project.setProjectId(projectId);
				return project;
			}
			catch(Exception e) { // Transaction rolled back
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
			}
		}
	
	public List<Project> fetchAllProjects() {
		// TODO Auto-generated method stub
		String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";
		
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				try(ResultSet rs = stmt.executeQuery()) {
					List<Project> projects = new LinkedList<>();
					
					// Shorter method:
					/*while(rs.next()) {
						projects.add(extract(rs, Project.class));
					}*/
					
					
					// Alternate method:
					while(rs.next()) {
					  Project project = new Project();
					  
					  project.setActualHours(rs.getBigDecimal("actual_hours"));
					  project.setDifficulty(rs.getObject("difficulty", Integer.class));
					  project.setEstimatedHours(rs.getBigDecimal("estimated_hours"));
					  project.setNotes(rs.getString("notes"));
					  project.setProjectId(rs.getObject("project_id", Integer.class));
					  project.setProjectName(rs.getString("project_name"));
					  
					 projects.add(project);
					  }
					 
					return projects;
				}
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);	
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
	}

	public Optional<Project> fetchProjectById(Integer projectId) {
		// TODO Auto-generated method stub
		
		String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			try {
				Project project = null;
				
				
				try(PreparedStatement stmt = conn.prepareStatement(sql)){
					setParameter(stmt, 1, projectId, Integer.class);
					
					try(ResultSet rs = stmt.executeQuery()){
						
						if(rs.next()) {
							project = extract(rs, Project.class);
						
							  }
					}
				}
			
				if(Objects.nonNull(project)) {
					
					project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
					project.getSteps().addAll(fetchStepsForProject(conn, projectId));
					project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
				}
				
				commitTransaction(conn);
				return Optional.ofNullable(project);				
		}
		catch(Exception e) {
			rollbackTransaction(conn);
			throw new DbException(e);
		}
	}
		catch(SQLException e ) {
			throw new DbException(e);
		}
	}
	
	private List<Category> fetchCategoriesForProject(Connection conn, 
			Integer projectId)throws SQLException {
		// @formatter:off
		String sql = ""
			+ "SELECT c.* FROM " + CATEGORY_TABLE + " c "
			+ "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
			+ "WHERE project_id = ?";
		// @formatter:on
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()) {
				List<Category> categories = new LinkedList<>();
				
				while(rs.next()) {
					categories.add(extract(rs, Category.class));
				}
				return categories;
			}
		}
	}
	/*
	 * In this step you will write the methods that will return materials, steps, and categories as Lists. 
	 * Each method is structured similarly. Since the Connection object is passed into each method, 
	 * you won't have to obtain the Connection from DbConnection.getConnection().  
	 * Also, you won't need to add catch blocks to the try-with-resource statements because the caller 
	 * makes the method calls within a try block. It won't hurt to catch the SQLException and turn it into 
	 * an unchecked exception as you have been doing. But it won't hurt to simply declare the exception in 
	 * the method signature either.
	 */
	private List<Material> fetchMaterialsForProject(Connection conn, 
			Integer projectId) throws SQLException {
		
		String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			setParameter(stmt, 1, projectId, Integer.class);
			try(ResultSet rs = stmt.executeQuery()) {
				List<Material> materials = new LinkedList<>();
				
				while(rs.next()) {
					materials.add(extract(rs, Material.class));
				}
				return materials;
			}
		}
	}
	private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException{
		String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()) {
				List<Step> steps = new LinkedList<>();
				
				while(rs.next()) {
					steps.add(extract(rs, Step.class));
				}
				return steps;
			}
		}
	}
	/*
	 * In modifyProjectDetails(), write the SQL statement to modify the project details. 
	 * Do not update the project ID – it should be part of the WHERE clause. Remember 
	 * to use question marks as parameter placeholders.
	 */
	public boolean modifyProjectDetails(Project project) {
		// TODO Auto-generated method stub
		// @formatter:off
		String sql = ""
			+ "UPDATE " + PROJECT_TABLE + " SET "
			+ "project_name = ?, "
			+ "estimated_hours = ?, "
			+ "actual_hours = ?, "
			+ "difficulty = ?, "
			+ "notes = ? "
			+ "WHERE project_id = ?";
		// @formatter:on
		
		/*
		 * Obtain the Connection and PreparedStatement using the appropriate 
		 * try-withresource and catch blocks. Start and rollback a transaction as usual. 
		 * Throw a DbException from each catch block. 
		 */
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				setParameter(stmt, 6, project.getProjectId(), Integer.class);
				
				boolean modified = stmt.executeUpdate() == 1;
				commitTransaction(conn);
				
				return modified;
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);											
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
	// Changes to the project DAO
		
	/*
	 * The deleteProject() method in the DAO is very similar to the modifyProjectDetails() method. 
	 * You will first create the SQL DELETE statement. Then, you will obtain the Connection and 
	 * PreparedStatement, and set the project ID parameter on the PreparedStatement. Then, you 
	 * will call executeUpdate() and verify that the return value is 1, indicating a successful 
	 * deletion. Finally, you will commit the transaction and return success or failure. 
	 */
	
	}
	public boolean deleteProject(Integer projectId) {
		/*
		 * In the method deleteProject(): 
		 * a. 	Write the SQL DELETE statement. Remember to use the placeholder for the project ID 
		 * in the WHERE clause. 
		 * b. 	Obtain a Connection and a PreparedStatement. Start, commit, and rollback a 
		 * transaction in the appropriate sections. 
		 * c. 	Set the project ID parameter on the 
		 * PreparedStatement. 
		 * d. 	Return true from the menu if executeUpdate() returns 1. 
		 */
		String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";
		
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, projectId, Integer.class);
				
				boolean deleted = stmt.executeUpdate() == 1;
				
				commitTransaction(conn);
				return deleted;
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
	}
}


		
		
