package TareaIndividualMendozaD;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class Main {
    private Connection databaseConnection;
    private JFrame mainFrame;
    private JComboBox<String> yearComboBox;
    private JTable driversTable;
    private DefaultTableModel tableModel;

    public Main() {
        // Establecer conexión a la base de datos PostgreSQL
        establishDBConnection();

        // Crear la interfaz gráfica
        setupGUI();
    }

    private void establishDBConnection() {
        try {
            String dbUrl = "jdbc:postgresql://localhost:5432/danilomdza?ssl=false";
            String dbUser = "danilomdza";
            String dbPassword = "danilomdza123";
            databaseConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            System.out.println("Conexión establecida con PostgreSQL.");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void setupGUI() {
        mainFrame = new JFrame("Tabla de Drivers por Año de Carrera");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1000, 500);

        // Combo box para seleccionar el año de carrera
        yearComboBox = new JComboBox<>();
        customizeComboBox(yearComboBox);
        populateYearComboBox();
        yearComboBox.addActionListener(e -> {
            // Cuando se seleccione un año, actualizar la tabla de corredores
            refreshTableData();
        });

        // Tabla para mostrar los datos de corredores y carreras
        tableModel = new DefaultTableModel();
        driversTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(driversTable);

        // Centra celdas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        driversTable.setDefaultRenderer(Object.class, centerRenderer);

        mainFrame.getContentPane().add(yearComboBox, BorderLayout.NORTH);
        mainFrame.getContentPane().add(tableScrollPane, BorderLayout.CENTER);

        mainFrame.setVisible(true);
    }

    private void customizeComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        comboBox.setRenderer(new BasicComboBoxRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    setBackground(new Color(59, 130, 246));
                    setForeground(Color.WHITE);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(Color.BLACK);
                }
                return this;
            }
        });

        comboBox.setPreferredSize(new Dimension(150, 30));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(59, 130, 246), 2));
    }

    private void populateYearComboBox() {
        for (int year = 1950; year <= 2018; year++) {
            yearComboBox.addItem(String.valueOf(year));
        }
    }

    private void refreshTableData() {
        try {
            String selectedYear = (String) yearComboBox.getSelectedItem();
            String sqlQuery = "SELECT d.forename || ' ' || d.surname AS driver_name, " +
                    "COUNT(CASE WHEN ds.position = 1 THEN 1 END) AS wins, " +
                    "SUM(ds.points) AS total_points, " +
                    "RANK() OVER (ORDER BY SUM(ds.points) DESC) AS rank " +
                    "FROM driver_standings ds " +
                    "JOIN races r ON ds.race_id = r.race_id " +
                    "JOIN drivers d ON ds.driver_id = d.driver_id " +
                    "WHERE r.year = ? " +
                    "GROUP BY d.driver_id, d.forename, d.surname";

            PreparedStatement preparedStatement = databaseConnection.prepareStatement(sqlQuery);
            preparedStatement.setInt(1, Integer.parseInt(selectedYear));

            ResultSet resultSet = preparedStatement.executeQuery();

            // pone columna piumba
            Vector<String> columnNames = new Vector<>();
            columnNames.add("Driver name");
            columnNames.add("Wins");
            columnNames.add("Total Points");
            columnNames.add("Rank");

            // pone fila
            Vector<Vector<Object>> rowData = new Vector<>();
            while (resultSet.next()) {
                Vector<Object> row = new Vector<>();
                row.add(resultSet.getString("driver_name"));
                row.add(resultSet.getInt("wins"));
                row.add(resultSet.getInt("total_points"));
                row.add(resultSet.getInt("rank"));
                rowData.add(row);
            }

            // actualiza la tabla
            tableModel.setDataVector(rowData, columnNames);

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
