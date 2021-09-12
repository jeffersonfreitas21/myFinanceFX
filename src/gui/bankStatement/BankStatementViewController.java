package gui.bankStatement;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import org.kordamp.bootstrapfx.BootstrapFX;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.entities.BankAccount;
import model.entities.BankStatement;
import model.entities.Moviment;
import model.service.BankAccountService;
import model.service.BankStatementService;
import model.service.MovimentService;
import utils.Alerts;
import utils.Utils;

public class BankStatementViewController implements Initializable{
	
	private BankStatementService service;
	public void setBankStatementService(BankStatementService service) {
		this.service = service;
	}
	
	@FXML
	private Button btnNew;
	@FXML
	public void onBtnNewAction(ActionEvent event) {
		loadModalView("/gui/bankStatement/BankStatementViewChooseAccount.fxml", "Escolha a conta para exibir o extrato", 270.0, 600.0, (BankStatementViewChooseAccountController controller) ->{
			controller.setBankStatementService(new BankStatementService());
			controller.setBankAccountService(new BankAccountService());
			controller.setMovimentService(new MovimentService());
			controller.loadAssociateObjects();
		});
	}



	@FXML
	private TableView<BankStatement> tblView;
	@FXML
	private TableColumn<BankStatement, Date> columnDate;
	@FXML
	private TableColumn<BankStatement, String> columnHistoric;
	@FXML
	private TableColumn<BankStatement, String> columnCredit;
	@FXML
	private TableColumn<BankStatement, Double> columnValue;
	@FXML
	private TableColumn<BankStatement, Double> columnBalance;
	
	private ObservableList<BankStatement> obsList = null;

	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializationNodes();
	}


	private void initializationNodes() {
	
		btnNew.getStyleClass().add("btn-info");
		columnDate.setCellValueFactory(new PropertyValueFactory<>("date"));
		Utils.formatTableColumnDate(columnDate, "dd/MM/yyyy");
		columnHistoric.setCellValueFactory(new PropertyValueFactory<>("historic"));
		columnValue.setCellValueFactory(new PropertyValueFactory<>("value"));
		Utils.formatTableColumnDouble(columnValue, 2);
		columnBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
		Utils.formatTableColumnDouble(columnBalance, 2);
		columnCredit.setCellValueFactory(v -> {
			String result = "";
			result = v.getValue().isCredit() ? "C" : "D";
			return new ReadOnlyStringWrapper(result);
		});
	}

	
	public void updateTableView(BankAccount bankAccount, Moviment moviment) {
		if(service == null) {
			throw new IllegalStateException("O servi�o n�o foi instanciado");
		}
		
		Double total = 0.0;
		
		List<BankStatement> list = service.findAllByAccountAndMoviment(bankAccount, moviment.getDateBeginner(), moviment.getDateFinish());
		for(BankStatement s : list) {
			
			if(s.isInitialValue()) {
				s.setBalance(s.getValue());
				total = s.getValue();
			}else {
				if(s.isCredit()) {
					s.setBalance(total + s.getValue());
					total = total + s.getValue();
				}else {
					s.setBalance(total - s.getValue());
					total = total - s.getValue();
				}
			}
			
		}
		obsList = FXCollections.observableArrayList(list);
		tblView.setItems(obsList);
		initializationNodes();
	}
	
	
	private synchronized <T> void loadModalView(String path, String title, double heigth, double width, Consumer<T> initialization) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
			Pane pane = loader.load();	
			Window window = btnNew.getScene().getWindow();
			Stage stage = new Stage();
			stage.setTitle(title);
			Scene scene = new Scene(pane);
			scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet()); 
			stage.setScene(scene);
			stage.setResizable(false);
			stage.initOwner(window);
			stage.initModality(Modality.WINDOW_MODAL);
			stage.setHeight(heigth);
			stage.setWidth(width);
			
			T controller = loader.getController();
			initialization.accept(controller);
			
			stage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
			Alerts.showAlert("Erro", "Erro ao abrir a janela", e.getMessage(), AlertType.ERROR);
		}
	}
	
	
}
