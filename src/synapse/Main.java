/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synapse;
import synapse.Algorithm.NeuralNetwork;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import static javax.swing.border.TitledBorder.CENTER;
import static javax.swing.border.TitledBorder.DEFAULT_POSITION;







/**
 *
 * @author quicksilver
 */
public class Main extends javax.swing.JFrame {
    

    /**
     * Creates new form Main
     */
    public Main() {
        initComponents();
        
    }
        private void resetData() {
        inputs.clear();
        trainData.clear();
        testData.clear();
        outputKinds.clear();
        trainTableModel.setColumnCount(0);
        trainTableModel.setRowCount(0);
        testTableModel.setColumnCount(0);
        testTableModel.setRowCount(0);
    }
            private Double round(Double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
        private Double normalize(Double input, Double min, Double max) {
        return round((input - min) / (max - min), 4);
    }
    
    private void initialData() {
        // Normalize expected output
        Double outputMin = Collections.min(outputKinds);
        Double outputMax = Collections.max(outputKinds);
        for (Double[] input : inputs) {
            input[input.length - 1] = normalize(input[input.length - 1], outputMin, outputMax);
        }
        for (int i = 0; i < outputKinds.size(); i++) {
            outputKinds.set(i, normalize(outputKinds.get(i), outputMin, outputMax));
        }
        // Split input into train & test
        int[] trainKindTimes = new int[outputKinds.size()];
        int[] testKindTimes = new int[outputKinds.size()];
        for (Double[] x : inputs) {
            Double output = x[x.length - 1];
            int i;
            for (i = 0; i < outputKinds.size(); i++)
                if (output.equals(outputKinds.get(i)))
                    break;
            if (trainKindTimes[i] == 0 || testKindTimes[i] > trainKindTimes[i] / 2) {
                ++trainKindTimes[i];
                trainData.add(x);
            } else {
                ++testKindTimes[i];
                testData.add(x);
            }
        }
    }
      private void alertBackground(JTextField textField, boolean alert) {
        if (alert)
            textField.setBackground(Color.PINK);
        else
            textField.setBackground(Color.WHITE);
    }
    
        private void startTrain(ArrayList<Double[]> inputs) {
        // set hidden
            hidden = hiddenTextField.getText(); 
        // set momentum
                            try {
                    alertBackground(momentumTextField, false);
                    momentum = Double.valueOf(momentumTextField.getText());
                } catch (NumberFormatException e) {
                    alertBackground(momentumTextField, true);
                    momentum = 0.5;
                }
        // set Rate
                try {
                    alertBackground(thresholdTextField, false);
                    threshold = Double.valueOf(thresholdTextField.getText());
                } catch (NumberFormatException e) {
                    alertBackground(thresholdTextField, true);
                    threshold = 0;
                }
        // set Threshold
                try {
                    alertBackground(thresholdTextField, false);
                    threshold = Double.valueOf(thresholdTextField.getText());
                } catch (NumberFormatException e) {
                    alertBackground(thresholdTextField, true);
                    threshold = 0;
                }
        //set Maxtimes
                  try {
                    alertBackground(maxTImesValue, false);
                    maxTimes = Integer.valueOf(maxTImesValue.getText());
                } catch (NumberFormatException e) {
                    alertBackground(maxTImesValue, true);
                    maxTimes = 1000;
                }
        // set MinError
                try {
                    alertBackground(minErrorTextField, false);
                    minError = Double.valueOf(minErrorTextField.getText());
                } catch (NumberFormatException e) {
                    alertBackground(minErrorTextField, true);
                    minError = 0.01;
                }
        // set Weight Minimum
                
                try {
                    if (Double.valueOf(wRangeMinValue.getText()) > maxRange)
                        alertBackground(wRangeMinValue, true);
                    else {
                        alertBackground(wRangeMinValue, false);
                        minRange = Double.valueOf(wRangeMinValue.getText());
                    }
                } catch (NumberFormatException e) {
                    alertBackground(wRangeMinValue, true);
                    minRange = -0.5f;
                }   
        // set Weight Maximum
                
                try {
                    if (Double.valueOf(wRangeMaxValue.getText()) < minRange)
                        alertBackground(wRangeMaxValue, true);
                    else {
                        alertBackground(wRangeMaxValue, false);
                        maxRange = Double.valueOf(wRangeMaxValue.getText());
                    }
                } catch (NumberFormatException e) {
                    alertBackground(wRangeMaxValue, true);
                    maxRange = 0.5f;
                }
        
        // 
        
        
        network = new NeuralNetwork(inputs, outputKinds, hidden, momentum,
                learningRate, threshold, minRange, maxRange);
        String[] resultTrain = network.run(maxTimes, minError).split(" ");
        timesValue.setText(resultTrain[0]);
        MSEValue.setText(resultTrain[1]);
        trainingValue.setText(resultTrain[2]);
        String resultTest = network.test(testData, maxTimes, minError);
        testingValue.setText(resultTest);
        coordinatePanel.repaint();
    }

    
        private void loadFile(JFileChooser fileChooser) {
        //coordinatePanel = new GPanel();    
        File loadedFile = fileChooser.getSelectedFile();
        loadValue.setText(loadedFile.getPath());
        //resetFrame();
        resetData();
        try (BufferedReader br = new BufferedReader(new FileReader(loadedFile))) {
            String line = br.readLine();
            while (line != null) {
                // Split by space or tab
                String[] lineSplit = line.split("\\s+");
                // Remove empty elements
                lineSplit = Arrays.stream(lineSplit).
                        filter(s -> (s != null && s.length() > 0)).
                        toArray(String[]::new);
                Double[] numbers = new Double[lineSplit.length + 1];
                numbers[0] = -1.0;
                for (int i = 1; i <= lineSplit.length; i++)
                    numbers[i] = Double.parseDouble(lineSplit[i - 1]);
                inputs.add(numbers);
                Double output = numbers[numbers.length - 1];
                if (!outputKinds.contains(output))
                    outputKinds.add(output);
                line = br.readLine();
            }
            initialData();
            ArrayList<String> header = new ArrayList<>();
            for (int i = 1; i < trainData.get(0).length - 1; i++)
                header.add("x" + i);
            header.add("yd");
            trainTableModel.setColumnIdentifiers(header.toArray());
            testTableModel.setColumnIdentifiers(header.toArray());
            for (Double[] x : trainData) {
                x = Arrays.copyOfRange(x, 1, x.length);
                trainTableModel.addRow(x);
            }
            for (Double[] x : testData) {
                x = Arrays.copyOfRange(x, 1, x.length);
                testTableModel.addRow(x);
            }
            trainTable.setModel(trainTableModel);
            testTable.setModel(testTableModel);
            // TODO - show y result at data table
            generateButton.setEnabled(true);
            startTrain(trainData);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
        
       private Double[] convertCoordinate(Double[] oldPoint) {
        Double[] newPoint = new Double[2];
        newPoint[0] = (oldPoint[0] * magnification) + 250;
        newPoint[1] = 250 - (oldPoint[1] * magnification);
        return newPoint;
    }
       
     private void createUIComponents() {
        //coordinatePanel = new GPanel();
        //coordinatePanel.setVisible(true);
       // zoomerSlider = new JSlider();
       // zoomerSlider.setBorder(
               // BorderFactory.createTitledBorder(null,
                        //Integer.toString(zoomerSlider.getValue()), CENTER, DEFAULT_POSITION));
    }

        
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFrame1 = new javax.swing.JFrame();
        jFrame2 = new javax.swing.JFrame();
        jFrame3 = new javax.swing.JFrame();
        jFrame4 = new javax.swing.JFrame();
        loadButton = new javax.swing.JButton();
        loadValue = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        testTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        trainTable = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        hiddenTextField = new javax.swing.JTextField();
        momentumTextField = new javax.swing.JTextField();
        learningTextField = new javax.swing.JTextField();
        thresholdTextField = new javax.swing.JTextField();
        wRangeMinValue = new javax.swing.JTextField();
        wRangeMaxValue = new javax.swing.JTextField();
        maxTImesValue = new javax.swing.JTextField();
        MSEValue = new javax.swing.JLabel();
        timesValue = new javax.swing.JLabel();
        trainingValue = new javax.swing.JLabel();
        testingValue = new javax.swing.JLabel();
        minErrorTextField = new javax.swing.JTextField();
        generateButton = new javax.swing.JButton();
        coordinatePanel = new javax.swing.JPanel();
        layoutPanel = new javax.swing.JPanel();
        graphButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        jFrame2.setFocusableWindowState(false);

        javax.swing.GroupLayout jFrame2Layout = new javax.swing.GroupLayout(jFrame2.getContentPane());
        jFrame2.getContentPane().setLayout(jFrame2Layout);
        jFrame2Layout.setHorizontalGroup(
            jFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame2Layout.setVerticalGroup(
            jFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jFrame3Layout = new javax.swing.GroupLayout(jFrame3.getContentPane());
        jFrame3.getContentPane().setLayout(jFrame3Layout);
        jFrame3Layout.setHorizontalGroup(
            jFrame3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame3Layout.setVerticalGroup(
            jFrame3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jFrame4Layout = new javax.swing.GroupLayout(jFrame4.getContentPane());
        jFrame4.getContentPane().setLayout(jFrame4Layout);
        jFrame4Layout.setHorizontalGroup(
            jFrame4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame4Layout.setVerticalGroup(
            jFrame4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        loadButton.setText("Load");
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        loadValue.setText("No data has been loaded");

        testTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(testTable);

        trainTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(trainTable);

        jLabel1.setText("Train Set");

        jLabel2.setText("Test Set");

        jLabel4.setText("Hidden Layer");

        jLabel5.setText("Momentum");

        jLabel6.setText("Learning Rate");

        jLabel7.setText("Initial Threshold");

        jLabel8.setText("Initial Weights Range");

        jLabel9.setText("Maximum Convergence");

        jLabel10.setText("MSE");

        jLabel11.setText("Epochs");

        jLabel12.setText("Error");

        jLabel13.setText("Training Accuracy");

        jLabel14.setText("Testing Accuracy");

        hiddenTextField.setText("4,4");
        hiddenTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hiddenTextFieldActionPerformed(evt);
            }
        });

        momentumTextField.setText("0.7");
        momentumTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                momentumTextFieldActionPerformed(evt);
            }
        });

        learningTextField.setText("0.1");

        thresholdTextField.setText("0");
        thresholdTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                thresholdTextFieldActionPerformed(evt);
            }
        });

        wRangeMinValue.setText("-0.5");

        wRangeMaxValue.setText("0.5");
        wRangeMaxValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wRangeMaxValueActionPerformed(evt);
            }
        });

        maxTImesValue.setText("1000");

        minErrorTextField.setText("0.01");

        generateButton.setText("Generate Results ");
        generateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layoutPanelLayout = new javax.swing.GroupLayout(layoutPanel);
        layoutPanel.setLayout(layoutPanelLayout);
        layoutPanelLayout.setHorizontalGroup(
            layoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 11, Short.MAX_VALUE)
        );
        layoutPanelLayout.setVerticalGroup(
            layoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 99, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout coordinatePanelLayout = new javax.swing.GroupLayout(coordinatePanel);
        coordinatePanel.setLayout(coordinatePanelLayout);
        coordinatePanelLayout.setHorizontalGroup(
            coordinatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(coordinatePanelLayout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(layoutPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(64, 64, 64))
        );
        coordinatePanelLayout.setVerticalGroup(
            coordinatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(coordinatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(layoutPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        graphButton.setText("Generate Graph");
        graphButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphButtonActionPerformed(evt);
            }
        });

        saveButton.setText("Save Results");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("Synapse - An interactive tool for tuning neural networks.");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(coordinatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(graphButton, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(120, 120, 120)
                                .addComponent(generateButton))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(learningTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel4)
                                            .addComponent(jLabel5))
                                        .addGap(33, 33, 33)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(momentumTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(hiddenTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(135, 135, 135)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel8)))
                            .addComponent(saveButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(loadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(82, 82, 82)
                        .addComponent(jLabel1)
                        .addGap(152, 152, 152))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(loadValue)
                                .addGap(9, 9, 9))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel10)
                                            .addComponent(jLabel11)
                                            .addComponent(jLabel12)
                                            .addComponent(jLabel13)
                                            .addComponent(jLabel14))
                                        .addGap(63, 63, 63)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(timesValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(trainingValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(testingValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(MSEValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(minErrorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(jLabel15)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(maxTImesValue, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(thresholdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(wRangeMinValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(wRangeMaxValue, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(17, 17, 17)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(57, 57, 57)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(144, 144, 144)
                                .addComponent(jLabel2))))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(loadButton)
                                    .addGap(18, 18, 18)
                                    .addComponent(loadValue))
                                .addComponent(coordinatePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(322, 322, 322)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel10)
                                            .addComponent(minErrorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel11)
                                            .addComponent(timesValue)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(20, 20, 20)
                                        .addComponent(jLabel3)
                                        .addGap(76, 76, 76)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel4)
                                            .addComponent(hiddenTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel7)
                                            .addComponent(thresholdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(46, 46, 46)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel5)
                                            .addComponent(momentumTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel9)
                                            .addComponent(maxTImesValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(56, 56, 56)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(jLabel6)
                                                    .addComponent(learningTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(42, 42, 42)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(jLabel8)
                                                    .addComponent(wRangeMinValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(wRangeMaxValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel12)
                                    .addComponent(MSEValue)
                                    .addComponent(graphButton)
                                    .addComponent(generateButton))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel13)
                                    .addComponent(trainingValue))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel14)
                                    .addComponent(testingValue))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(saveButton)
                                .addGap(23, 23, 23)
                                .addComponent(jLabel15)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void wRangeMaxValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wRangeMaxValueActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_wRangeMaxValueActionPerformed

    private void momentumTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_momentumTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_momentumTextFieldActionPerformed

    private void thresholdTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_thresholdTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_thresholdTextFieldActionPerformed

    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
            //GPanel coordinatePanel = new GPanel();
            //jFrame0.add(coordinatePanel);
            //jFrame0.pack();
            //jFrame0.setVisible(true);
            coordinatePanel.setVisible(true);
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files(*.txt)", "txt", "text");
            fileChooser.setFileFilter(filter);
            if (fileChooser.showOpenDialog(layoutPanel) == JFileChooser.APPROVE_OPTION) {
                loadFile(fileChooser);
            }
    }//GEN-LAST:event_loadButtonActionPerformed

    private void generateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateButtonActionPerformed
            startTrain(trainData);
    }//GEN-LAST:event_generateButtonActionPerformed

    private void hiddenTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hiddenTextFieldActionPerformed
           hidden = hiddenTextField.getText();  
    }//GEN-LAST:event_hiddenTextFieldActionPerformed

    private void graphButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_graphButtonActionPerformed
        // TODO add your handling code here:
        coordinatePanel = new GPanel();
        //Main window = new Main();
        coordinatePanel.setSize(3000,3000);
        jFrame4.add(coordinatePanel);
        jFrame4.validate();
        
        jFrame4.pack();
        jFrame4.setVisible(true);
    }//GEN-LAST:event_graphButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        // TODO add your handling code here:
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	LocalDateTime now = LocalDateTime.now();
	String date = dtf.format(now).toString();
        JOptionPane.showMessageDialog(jFrame1, date);
        
        String path = "/home/quicksilver/NetBeansProjects/Synapse/src/synapse/Users/logs/log-" + date +".txt";
        try{
            PrintWriter writer = new PrintWriter(path, "UTF-8");
        
        writer.println("Dataset trained on " + loadValue.getText() );
        writer.println("Training Accuracy " + trainingValue.getText());
        writer.println("Training Accuracy " + testingValue.getText());
        writer.println("Number of Epochs " + maxTImesValue.getText());
        writer.println("************** Model Summary **************");
        writer.println("Number of hidden layers" + hiddenTextField.getText());
        writer.println("Momentum " + momentumTextField.getText());
        writer.println("Optimizer :- SGD with momentum");
        writer.println("Learning rate " + learningTextField.getText());
        
        
        
  
        writer.close();
    }//GEN-LAST:event_saveButtonActionPerformed
catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(jFrame1, "Unexpected error. File wasn't found");
}
catch (UnsupportedEncodingException e) {
            JOptionPane.showMessageDialog(jFrame1, "Unexpected error. File wasn't found");
}
        
    }
        
        
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                
               //Main window = new Main();
                //System.out.println("Hello");
                //coordinatePanel = new GPanel();
                //coordinatePanel.setSize(100,100);
                //window.add(coordinatePanel);

                //window.pack();
                //window.setVisible(true);

	//Main().setVisible(true);
                
                //new Main().setVisible(true);
                //jFrame1.setVisible(true);
                
                //coordinatePanel = new GPanel();
            }
        });
    }
    private class GPanel extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            // Draw output kind area
            if (drawMode && size > 0 && inputs.size() > 0 && inputs.get(0).length == 4) {
                ArrayList<Double[]> drawInputs = new ArrayList<>();
                for (Double x = -250.0; x <= 250; x += size) {
                    for (Double y = -250.0; y <= 250; y += size) {
                        drawInputs.add(new Double[]{-1.0, x / magnification, y / magnification});
                    }
                }
                int id[] = network.getOutputKind(drawInputs, maxTimes, minError);
                int i = 0;
                for (Double x = -250.0; x <= 250; x += size) {
                    for (Double y = -250.0; y <= 250; y += size) {
                        g2.setColor(colorArray[colorArray.length - id[i] - 1]);
                        Double[] point = convertCoordinate(new Double[]{x / magnification, y / magnification});
                        Rectangle2D rect = new Rectangle2D.Double(point[0], point[1], size, size);
                        g2.fill(rect);
                        i++;
                    }
                }
            }
            g.setColor(Color.black);
            g.drawLine(250, 0, 250, 500);
            g.drawLine(0, 250, 500, 250);
            g2.setColor(Color.black);
            // Draw scale
            for (Double i = 250.0; i >= 0; i -= 5.0 * magnification / 10) {
                drawScale(g2, i);
            }
            for (Double i = 250.0; i <= 500; i += 5.0 * magnification / 10) {
                drawScale(g2, i);
            }
            g2.setStroke(new BasicStroke(3));
            // Draw mouse position
            if (mouse != null) {
                Double mouse_x = (mouse.getX() - 250) / magnification;
                Double mouse_y = (250 - mouse.getY()) / magnification;
                g2.drawString("(" + df.format(mouse_x) + ", " + df.format(mouse_y) + ")", 420, 20);
            }
            // Draw point of file
            if (inputs.size() > 0 && inputs.get(0).length == 4) {
                for (Double[] x : inputs) {
                    Double[] point = convertCoordinate(new Double[]{x[1], x[2]});
                    for (int i = 0; i < outputKinds.size(); i++) {
                        Double outputKind = outputKinds.get(i);
                        if (x[x.length - 1].equals(outputKind)) {
                            g2.setColor(colorArray[i]);
                            break;
                        }
                    }
                    g2.draw(new Line2D.Double(point[0], point[1], point[0], point[1]));
                }
            }
            // TODO: Draw line of decision boundary
        }

        private void drawScale(Graphics2D g2, Double i) {
            Double[] top, btn;
            Double scaleLength = (i % (5.0 * magnification / 5) == 0) ? 2.0 * magnification / 20 : 1.0 * magnification / 20;
            top = convertCoordinate(new Double[]{(i - 250) / magnification, scaleLength / magnification});
            btn = convertCoordinate(new Double[]{(i - 250) / magnification, -scaleLength / magnification});
            g2.draw(new Line2D.Double(top[0], top[1], btn[0], btn[1]));
            top = convertCoordinate(new Double[]{-scaleLength / magnification, (250 - i) / magnification});
            btn = convertCoordinate(new Double[]{scaleLength / magnification, (250 - i) / magnification});
            g2.draw(new Line2D.Double(top[0], top[1], btn[0], btn[1]));
        }
    }
    
    



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel MSEValue;
    private javax.swing.JPanel coordinatePanel;
    private javax.swing.JButton generateButton;
    private javax.swing.JButton graphButton;
    private javax.swing.JTextField hiddenTextField;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JFrame jFrame2;
    private javax.swing.JFrame jFrame3;
    private javax.swing.JFrame jFrame4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel layoutPanel;
    private javax.swing.JTextField learningTextField;
    private javax.swing.JButton loadButton;
    private javax.swing.JLabel loadValue;
    private javax.swing.JTextField maxTImesValue;
    private javax.swing.JTextField minErrorTextField;
    private javax.swing.JTextField momentumTextField;
    private javax.swing.JButton saveButton;
    private javax.swing.JTable testTable;
    private javax.swing.JLabel testingValue;
    private javax.swing.JTextField thresholdTextField;
    private javax.swing.JLabel timesValue;
    private javax.swing.JTable trainTable;
    private javax.swing.JLabel trainingValue;
    private javax.swing.JTextField wRangeMaxValue;
    private javax.swing.JTextField wRangeMinValue;
    // End of variables declaration//GEN-END:variables
    private DecimalFormat df = new DecimalFormat("####0.00");
    private Color[] colorArray = {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.CYAN, Color.PINK};
    private NeuralNetwork network;
    private static ArrayList<Double[]> inputs = new ArrayList<>();
    private ArrayList<Double[]> trainData = new ArrayList<>();
    private ArrayList<Double[]> testData = new ArrayList<>();
    private ArrayList<Double> outputKinds = new ArrayList<>();
    private Point mouse;
    private int maxTimes = 1000;
    private static int magnification = 50;
    private String hidden = "4,4";
    private double momentum = 0.7;
    private double learningRate = 0.1;
    private double threshold = 0;
    private double minRange = -0.5;
    private double maxRange = 0.5;
    private double minError = 0.01;
    private static double size = 20.0;
    private static boolean drawMode = true;
    private DefaultTableModel trainTableModel = new DefaultTableModel();
    private DefaultTableModel testTableModel = new DefaultTableModel();
    
}

