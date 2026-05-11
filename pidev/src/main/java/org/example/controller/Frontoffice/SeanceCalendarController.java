package org.example.controller.Frontoffice;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.entities.Seance;
import org.example.services.SeanceService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SeanceCalendarController {

    @FXML private Label monthYearLabel;
    @FXML private GridPane dayNamesGrid;
    @FXML private GridPane calendarGrid;
    @FXML private Label selectedDateLabel;
    @FXML private VBox seancesDayList;

    private final SeanceService seanceService = new SeanceService();
    private YearMonth currentYearMonth;
    private List<Seance> allSeances;
    private final DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
    private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH);

    @FXML
    public void initialize() {
        allSeances = seanceService.afficherSeances();
        currentYearMonth = YearMonth.now();
        
        setupDayNames();
        drawCalendar();
    }

    private void setupDayNames() {
        String[] days = {"LUN", "MAR", "MER", "JEU", "VEN", "SAM", "DIM"};
        for (int i = 0; i < days.length; i++) {
            Label label = new Label(days[i]);
            label.setPrefWidth(110);
            label.setAlignment(Pos.CENTER);
            label.setFont(Font.font("System", FontWeight.BOLD, 14));
            label.setTextFill(Color.web("#7f8c8d"));
            dayNamesGrid.add(label, i, 0);
        }
    }

    private void drawCalendar() {
        calendarGrid.getChildren().clear();
        monthYearLabel.setText(currentYearMonth.format(monthYearFormatter).toUpperCase());

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 = Mon, 7 = Sun
        int daysInMonth = currentYearMonth.lengthOfMonth();

        int row = 0;
        int col = dayOfWeek - 1;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            VBox dayCell = createDayCell(date);
            calendarGrid.add(dayCell, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
        
        // Default select today if in current month
        if (currentYearMonth.equals(YearMonth.now())) {
            showSeancesForDate(LocalDate.now());
        }
    }

    private VBox createDayCell(LocalDate date) {
        VBox cell = new VBox(5);
        cell.setPrefSize(110, 80);
        cell.setAlignment(Pos.TOP_CENTER);
        cell.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #ecf0f1; -fx-border-width: 1; -fx-border-radius: 12; -fx-cursor: hand;");
        
        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        if (date.equals(LocalDate.now())) {
            dayLabel.setTextFill(Color.web("#0b7a8f"));
            cell.setStyle(cell.getStyle() + "-fx-border-color: #0b7a8f; -fx-border-width: 2;");
        }

        List<Seance> seancesOnDate = allSeances.stream()
                .filter(s -> s.getDateDebut().toLocalDate().equals(date))
                .collect(Collectors.toList());

        if (!seancesOnDate.isEmpty()) {
            VBox indicator = new VBox(2);
            indicator.setAlignment(Pos.CENTER);
            Label countLabel = new Label(seancesOnDate.size() + " séance(s)");
            countLabel.setFont(Font.font(10));
            countLabel.setTextFill(Color.WHITE);
            indicator.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #0b7a8f 0%, #065f6e 100%); -fx-background-radius: 6; -fx-padding: 3 8;");
            indicator.getChildren().add(countLabel);
            cell.getChildren().addAll(dayLabel, indicator);
        } else {
            cell.getChildren().add(dayLabel);
        }

        cell.setOnMouseClicked(e -> showSeancesForDate(date));
        
        cell.setOnMouseEntered(e -> cell.setStyle(cell.getStyle() + "-fx-background-color: #f8f9fa;"));
        cell.setOnMouseExited(e -> cell.setStyle(cell.getStyle().replace("-fx-background-color: #f8f9fa;", "-fx-background-color: white;")));

        return cell;
    }

    private void showSeancesForDate(LocalDate date) {
        selectedDateLabel.setText("Séances du " + date.format(dayFormatter));
        seancesDayList.getChildren().clear();

        List<Seance> seancesOnDate = allSeances.stream()
                .filter(s -> s.getDateDebut().toLocalDate().equals(date))
                .collect(Collectors.toList());

        if (seancesOnDate.isEmpty()) {
            seancesDayList.getChildren().add(new Label("Aucune séance prévue."));
        } else {
            for (Seance s : seancesOnDate) {
                Label label = new Label("🕒 " + s.getDateDebut().toLocalTime() + " - " + s.getTitre());
                label.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                seancesDayList.getChildren().add(label);
            }
        }
    }

    @FXML
    private void handlePreviousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        drawCalendar();
    }

    @FXML
    private void handleNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        drawCalendar();
    }

    @FXML
    private void handleToday() {
        currentYearMonth = YearMonth.now();
        drawCalendar();
        showSeancesForDate(LocalDate.now());
    }
}
