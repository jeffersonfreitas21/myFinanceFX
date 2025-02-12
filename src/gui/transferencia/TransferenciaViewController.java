package gui.transferencia;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import org.kordamp.bootstrapfx.BootstrapFX;

import database.exceptions.DatabaseException;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import model.entities.Transferencia;
import model.service.BankAccountService;
import model.service.TransferenciaService;
import utils.Alerts;
import utils.Utils;

public class TransferenciaViewController implements Initializable{
	
	private TransferenciaService service;
	public void setTransferenciaService(TransferenciaService service) {
		this.service = service;
	}
	
	@FXML
	private Button btnNew;
	@FXML
	public void onBtnNewAction(ActionEvent event) {
		loadModalView("/gui/transferencia/TransferenciaViewRegister.fxml", "Cadastro de transferencia", 410.0, 600.0, (TransferenciaViewRegisterController controller) ->{
			controller.setTransferenciaService(new TransferenciaService());
			controller.setTransferencia(new Transferencia());
			controller.setBankAccountService(new BankAccountService());
			controller.loadAssociateObjects();
		});
	}



	@FXML
	private TableView<Transferencia> tblView;
	@FXML
	private TableColumn<Transferencia, Date> columnDate;
	@FXML
	private TableColumn<Transferencia, String> columnObservation;
	@FXML
	private TableColumn<Transferencia, String> columnOrigin;
	@FXML
	private TableColumn<Transferencia, String> columnDestination;
	@FXML
	private TableColumn<Transferencia, Double> columnValue;
	@FXML
	private TableColumn<Transferencia, Transferencia> tblColumnDELETE;
	
	private ObservableList<Transferencia> obsList = null;

	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializationNodes();
	}


	private void initializationNodes() {
	
		btnNew.getStyleClass().add("btn-primary");
		columnDate.setCellValueFactory(new PropertyValueFactory<>("date"));
		Utils.formatTableColumnDate(columnDate, "dd/MM/yyyy");
		columnObservation.setCellValueFactory(new PropertyValueFactory<>("observation"));
		columnValue.setCellValueFactory(new PropertyValueFactory<>("value"));
		Utils.formatTableColumnDouble(columnValue, 2);
		columnOrigin.setCellValueFactory(v -> {
			String result = "";
			result = v.getValue().getOriginAccount().getCode();
			return new ReadOnlyStringWrapper(result);
		});
		columnDestination.setCellValueFactory(v -> {
			String result = "";
			result = v.getValue().getDestinationAccount().getCode();
			return new ReadOnlyStringWrapper(result);
		});
	}

	
	public void updateTableView() {
		if(service == null) {
			throw new IllegalStateException("O servi�o n�o foi instanciado");
		}
		List<Transferencia> list = service.findAll();
		obsList = FXCollections.observableArrayList(list);
		tblView.setItems(obsList);
		initializationNodes();
		initRemoveButtons();
	}
	
	
	private void initRemoveButtons() {
		tblColumnDELETE.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tblColumnDELETE.setCellFactory(param -> new TableCell<Transferencia, Transferencia>() {
			private final Button button = new Button();
			
			@Override
			protected void updateItem(Transferencia entity, boolean empty) {
				button.setGraphic(new ImageView("/assets/icons/trash16.png"));
				button.setStyle(" -fx-background-color:transparent;");
				button.setCursor(Cursor.HAND);
				super.updateItem(entity, empty);
				if(entity == null || entity.isClose()) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(e -> removeEntity(entity));
			}
		});
	}
	
	
	private void removeEntity(Transferencia entity) {
		Optional<ButtonType> result = Alerts.showConfirmation("Confirma��o", "Voc� tem certeza que deseja remover este item?");
		
		if(result.get() == ButtonType.OK) {
			if(service == null) {
				throw new IllegalStateException("Servi�o n�o instanciado");
			}
			try {
				service.remove(entity);
				updateTableView();
			} catch (DatabaseException e) {
				e.printStackTrace();
				Alerts.showAlert("Erro ao remover registro", null, e.getMessage(), AlertType.ERROR);
			}
		}
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
			
			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					updateTableView();
				}
			});
			
			
			stage.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
			Alerts.showAlert("Erro", "Erro ao abrir a janela", e.getMessage(), AlertType.ERROR);
		}
	}
	
	
}
