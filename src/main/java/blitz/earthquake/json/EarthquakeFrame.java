package blitz.earthquake.json;

import blitz.earthquake.EarthquakeService;
import blitz.earthquake.EarthquakeServiceFactory;
import hu.akarnokd.rxjava3.swing.SwingSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class EarthquakeFrame extends JFrame {

    private JList<String> jlist = new JList<>();
    private JRadioButton oneHour = new JRadioButton("One hour");
    private JRadioButton sigMonth = new JRadioButton("30 days");
    private Disposable disposable;
    private FeatureCollection currentFeatures;


    public EarthquakeFrame() {
        setTitle("EarthquakeFrame");
        setSize(300, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        JPanel radioButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioButtonPanel.add(oneHour);
        radioButtonPanel.add(sigMonth);

        ButtonGroup group = new ButtonGroup();
        group.add(oneHour);
        group.add(sigMonth);

        add(radioButtonPanel, BorderLayout.PAGE_START);
        add(jlist, BorderLayout.CENTER);

        EarthquakeService service = new EarthquakeServiceFactory().getService();

        oneHour.addActionListener(e -> fetchData(service.oneHour()));
        sigMonth.addActionListener(e -> fetchData(service.sigMonth()));

        jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jlist.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && currentFeatures != null) {
                int index = jlist.getSelectedIndex();
                if (index != -1) {
                    Feature feature = currentFeatures.features[index];
                    double latitude = feature.geometry.coordinates[1];
                    double longitude = feature.geometry.coordinates[0];
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        try {
                            Desktop.getDesktop().browse(new URI(
                                    "https://www.google.com/maps/search/?api=1&query="
                                            + latitude + "," + longitude));
                        } catch (IOException | URISyntaxException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
        });

                fetchData(service.oneHour());
            }

    private void fetchData(Single<FeatureCollection> single) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        disposable = single
                .subscribeOn(Schedulers.io())
                .observeOn(SwingSchedulers.edt())
                .subscribe(
                        this::handleResponse,
                        Throwable::printStackTrace
                );
    }

    private void handleResponse(FeatureCollection response) {
        currentFeatures = response;
        String[] listData = new String[response.features.length];
        for (int i = 0; i < response.features.length; i++) {
            Feature feature = response.features[i];
            listData[i] = feature.properties.mag + " " + feature.properties.place;
        }
        jlist.setListData(listData);
    }

    public static void main(String[] args) {
        new EarthquakeFrame().setVisible(true);
    }
}