// Definindo o pacote da classe
package player;

// Importando as classes necessárias para manipulação de arquivos e interface gráfica Swing
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.sound.sampled.*;
import java.io.IOException;

// A classe Player agora herda de JFrame para criar uma interface gráfica e de Thread para tocar a música
public class Player extends JFrame implements Runnable {
    // Variáveis para armazenar o Clip, os componentes da interface gráfica e a playlist
    private Clip clip;
    private JLabel labelFileName;
    private JLabel labelTime;
    private JSlider sliderTime;
    private JButton buttonPlayPause;
    private JButton buttonStop;
    private JButton buttonNext;
    private JButton buttonPrevious;
    private JButton buttonRepeat;
    private JList<File> listUpcomingFiles;
    private ArrayList<File> playlist;
    private int currentSongIndex;

    // Método construtor
    public Player() {
        // Configurando o JFrame
        this.setSize(500, 200);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        
        // Configurando o layout do JFrame
        this.setLayout(new GridLayout(3, 1));

        // Criando a playlist
        playlist = new ArrayList<>();
        currentSongIndex = -1;

        // Criando um menu para selecionar o arquivo de música
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("Arquivo");
        JMenuItem menuItemOpen = new JMenuItem("Abrir");
        menuFile.add(menuItemOpen);
        menuBar.add(menuFile);
        this.setJMenuBar(menuBar);

        // Adicionando um listener para o item de menu "Abrir"
        menuItemOpen.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File[] files = fileChooser.getSelectedFiles();
                for (File file : files) {
                    playlist.add(file);
                }
                if (clip == null || !clip.isRunning()) {
                    currentSongIndex = 0;
                    playMusic(playlist.get(currentSongIndex));
                }
            }
        });

        // Criando os componentes da interface gráfica
        labelFileName = new JLabel("Arquivo: ");
        this.add(labelFileName);

        sliderTime = new JSlider(0, 100, 0);
        sliderTime.setEnabled(false);
        this.add(sliderTime);

        labelTime = new JLabel("00:00 / 00:00");
        this.add(labelTime);

        JPanel panelButtons = new JPanel();
        buttonPlayPause = new JButton("Play");
        buttonStop = new JButton("Stop");
        buttonNext = new JButton("Avançar");
        buttonPrevious = new JButton("Retroceder");
        buttonRepeat = new JButton("Repetir");
        panelButtons.add(buttonPlayPause);
        panelButtons.add(buttonStop);
        panelButtons.add(buttonNext);
        panelButtons.add(buttonPrevious);
        panelButtons.add(buttonRepeat);
        this.add(panelButtons);

        // Adicionando um listener para o botão de play/pause
        buttonPlayPause.addActionListener(e -> {
            if (playlist.isEmpty()) {
                playMusic(new File("resources/musica.wav"));
            } else if (clip != null) {
                if (clip.isRunning()) {
                    clip.stop();
                    buttonPlayPause.setText("Play");
                } else {
                    clip.start();
                    buttonPlayPause.setText("Pause");
                }
            }
        });

        // Adicionando um listener para o botão de stop
        buttonStop.addActionListener(e -> {
            if (clip != null) {
                clip.stop();
                clip.setFramePosition(0);
                buttonPlayPause.setText("Play");
                sliderTime.setValue(0);
                labelTime.setText("00:00 / " + clip.getMicrosecondLength() / 10000000 + ":00");
            }
        });

        // Adicionando um listener para o botão de avançar
        buttonNext.addActionListener(e -> {
            if (!playlist.isEmpty() && currentSongIndex < playlist.size() - 1) {
                playMusic(playlist.get(++currentSongIndex));
            }
        });

        // Adicionando um listener para o botão de retroceder
        buttonPrevious.addActionListener(e -> {
            if (!playlist.isEmpty() && currentSongIndex > 0) {
                playMusic(playlist.get(--currentSongIndex));
            }
        });

        // Adicionando um listener para o botão de repetir
        buttonRepeat.addActionListener(e -> {
            if (clip != null) {
                clip.setFramePosition(0);
                clip.start();
                buttonPlayPause.setText("Pause");
            }
        });

        // Criando e adicionando a lista dos próximos arquivos
        listUpcomingFiles = new JList<>();
        listUpcomingFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.add(new JScrollPane(listUpcomingFiles));

        // Adicionando um listener para a lista de arquivos
        listUpcomingFiles.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = listUpcomingFiles.getSelectedIndex();
                if (selectedIndex != -1) {
                    currentSongIndex = selectedIndex;
                    playMusic(playlist.get(currentSongIndex));
                }
            }
        });

        // Mostrando a interface gráfica
        this.setVisible(true);
    }

    // Método principal
    public static void main(String[] args) {
        // Lançando a interface gráfica do Swing
        new Thread(new Player()).start();
    }

    // Método run que é chamado quando a thread é iniciada
    @Override
    public void run() {
        // Esperando o usuário selecionar um arquivo de música
        while (true) {
            if (clip != null && clip.isRunning()) {
                long micros = clip.getMicrosecondPosition();
                long totalMicros = clip.getMicrosecondLength();
                long seconds = micros / 1000000;
                long totalSeconds = totalMicros / 1000000;
                sliderTime.setValue((int) seconds);
                String currentTime = String.format("%02d:%02d", seconds / 60, seconds % 60);
                String totalTime = String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);
                labelTime.setText(currentTime + " / " + totalTime);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para tocar a música
    private void playMusic(File file) {
        // Tentativa de criar um AudioInputStream e um Clip para a música
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            // Configurando o valor máximo do slider para o tempo total de reprodução em segundos
            sliderTime.setMaximum((int) (clip.getMicrosecondLength() / 1000000));
            // Tocando a música
            clip.start();
            sliderTime.setEnabled(true);
            long totalMicros = clip.getMicrosecondLength();
            long totalSeconds = totalMicros / 1000000;
            String totalTime = String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);
            labelTime.setText("00:00 / " + totalTime);
            buttonPlayPause.setText("Pause");

            // Atualizando a lista de arquivos
            updateUpcomingFiles();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            // Se ocorrer um erro, mostra uma mensagem de erro
            JOptionPane.showMessageDialog(null, "Erro ao tocar a música: " + e.getMessage());
        }
    }

    // Método para atualizar a lista de arquivos
    private void updateUpcomingFiles() {
        // Limpando a lista de arquivos
        listUpcomingFiles.clearSelection();
        listUpcomingFiles.setListData(new File[0]);

        // Adicionando os próximos arquivos à lista
        if (currentSongIndex < playlist.size() - 1) {
            int filesToAdd = Math.min(10, playlist.size() - currentSongIndex - 1);
            File[] upcomingFiles = new File[filesToAdd];
            for (int i = 0; i < filesToAdd; i++) {
                upcomingFiles[i] = playlist.get(currentSongIndex + i + 1);
            }
            listUpcomingFiles.setListData(upcomingFiles);
        }
    }
}
