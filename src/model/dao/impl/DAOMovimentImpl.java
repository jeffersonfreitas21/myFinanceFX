package model.dao.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import database.Database;
import database.exceptions.DatabaseException;
import model.dao.DAOMoviment;
import model.entities.Moviment;
import utils.DefaultMessages;

public class DAOMovimentImpl implements DAOMoviment{
	
	private Connection conn;
	
	public DAOMovimentImpl(Connection conn) {
		this.conn = conn;
	}

	
	@Override
	public Integer insert(Moviment entity) {
		validaCamposObrigatorios(entity);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "INSERT INTO moviment (date_beginner, name, date_finish, value_beginner, balance_moviment, value_finish, value_poupanca, value_aplicacao, value_resgate, closed) VALUES (?, upper(?), ?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setDate(1, new java.sql.Date(entity.getDateBeginner().getTime()));
			stmt.setString(2, entity.getName());
			stmt.setDate(3, new java.sql.Date(entity.getDateFinish().getTime()));
			stmt.setDouble(4, entity.getValueBeginner());
			stmt.setDouble(5, entity.getBalanceMoviment());
			stmt.setDouble(6, entity.getValueFinish());
			stmt.setDouble(7, entity.getValuePoupanca());
			stmt.setDouble(8, entity.getValueAplicacao());
			stmt.setDouble(9, entity.getValueResgate());
			stmt.setBoolean(10, entity.isClosed());
			int result = stmt.executeUpdate();
			if(result < 1) {
				throw new DatabaseException(DefaultMessages.getMsgErroSalvar() + ". Nenhuma linha afetada");
			}
			
			rs = stmt.getGeneratedKeys();
			if(rs.next()) {
				return rs.getInt("id");
			}
			return null;
		} catch (SQLException | NullPointerException e) {
			e.printStackTrace();
			throw new DatabaseException(DefaultMessages.getMsgErroSalvar());
		}finally {
			Database.closeStatement(stmt);
			Database.closeResultSet(rs);
		}
	}


	
	@Override
	public Integer update(Moviment entity) {
		validaCamposObrigatorios(entity);

		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "UPDATE moviment SET date_beginner = ?, name = upper(?), date_finish = ?, value_beginner = ?, balance_moviment = ?, value_finish = ?, value_poupanca = ?, value_aplicacao = ?, value_resgate = ?, closed = ? WHERE id = ?";
		try {
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setDate(1, new java.sql.Date(entity.getDateBeginner().getTime()));
			stmt.setString(2, entity.getName());
			stmt.setDate(3, new java.sql.Date(entity.getDateFinish().getTime()));
			stmt.setDouble(4, entity.getValueBeginner());
			stmt.setDouble(5, entity.getBalanceMoviment());
			stmt.setDouble(6, entity.getValueFinish());
			stmt.setDouble(7, entity.getValuePoupanca());
			stmt.setDouble(8, entity.getValueAplicacao());
			stmt.setDouble(9, entity.getValueResgate());
			stmt.setBoolean(10, entity.isClosed());
			stmt.setInt(11, entity.getId());
			int result = stmt.executeUpdate();
			
			if(result < 1) {
				throw new DatabaseException(DefaultMessages.getMsgErroAtualizar() + ". Nenhuma linha afetada");
			}
			
			rs = stmt.getGeneratedKeys();
			if(rs.next()) {
				return rs.getInt("id");
			}
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(DefaultMessages.getMsgErroAtualizar());
		}finally {
			Database.closeStatement(stmt);
		}
	}

	
	@Override
	public Integer deleteById(Integer id) {
		PreparedStatement stmt = null;
		String sql = "DELETE FROM moviment WHERE id = ?";
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			int result = stmt.executeUpdate();
			
			if(result < 1) {
				throw new DatabaseException(DefaultMessages.getMsgErroDeletar() + ". Nenhuma linha afetada");
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(DefaultMessages.getMsgErroDeletar());
		}finally {
			Database.closeStatement(stmt);
		}
	}

	
	@Override
	public Moviment findById(Integer id) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "SELECT * FROM moviment WHERE id = ?";
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				Moviment Moviment = instantiateMoviment(rs); 
				return Moviment;
			}
			return null;

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(DefaultMessages.getMsgErroFindby() + ". C�digo n� " + id);
		}finally {
			Database.closeStatement(stmt);
			Database.closeResultSet(rs);
		}
	}

	
	@Override
	public List<Moviment> findAllOrderByDateBeginner() {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "SELECT * FROM moviment ORDER BY date_beginner desc";
		try {
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			
			List<Moviment> list = new ArrayList<>();
			
			while(rs.next()) {
				Moviment moviment = instantiateMoviment(rs); 
				list.add(moviment);
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(DefaultMessages.getMsgErroFindall());
		}finally {
			Database.closeStatement(stmt);
			Database.closeResultSet(rs);
		}
	}


	private Moviment instantiateMoviment(ResultSet rs) throws SQLException{
		Moviment moviment = new Moviment();
		moviment.setBalanceMoviment(rs.getDouble("balance_moviment"));
		moviment.setClosed(rs.getBoolean("closed"));
		moviment.setDateBeginner(new Date(rs.getDate("date_beginner").getTime()));
		moviment.setDateFinish(new Date(rs.getDate("date_finish").getTime()));
		moviment.setId(rs.getInt("id"));
		moviment.setName(rs.getString("name"));
		moviment.setValueBeginner(rs.getDouble("value_beginner"));
		moviment.setValueFinish(rs.getDouble("value_finish"));
		moviment.setValuePoupanca(rs.getDouble("value_poupanca"));
		moviment.setValueAplicacao(rs.getDouble("value_aplicacao"));
		moviment.setValueResgate(rs.getDouble("value_resgate"));
		return moviment;
	}


	@Override
	public List<Moviment> findByAllOpenMoviment() {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "SELECT * FROM moviment WHERE closed = false";
		try {
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			List<Moviment> list = new ArrayList<>();
			while(rs.next()) {
				Moviment entity = instantiateMoviment(rs);
				list.add(entity);
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException(DefaultMessages.getMsgErroFindall());
		}finally {
			Database.closeStatement(stmt);
			Database.closeResultSet(rs);
		}
	}
	
	private void validaCamposObrigatorios(Moviment entity) {
		if(entity.getDateBeginner() == null || entity.getDateFinish() == null || entity.getName().equals("") || entity.getValueBeginner() == null) {
			throw new DatabaseException("Existe campos obrigat�rios nulos");
		}
	}

}
