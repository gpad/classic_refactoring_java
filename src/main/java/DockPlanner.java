import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

class Reservation {

    int id;
    byte[] document;
    String first_name;
    String last_name;
    String veichle_plate;
    String type;
    LocalDateTime start_at;
    LocalDateTime end_at;
}

class Config {

    // minutes
    int timeForPallet() {
        return 30;
    }

    String shared_folder_for_email() {
        return "./emailToSend";
    }

    String reservation_folder() {
        return "./reservations";
    }
}

class DockPlanner {
    List<Reservation> _loading = new ArrayList<>();
    List<Reservation> _unloading = new ArrayList<>();


    int calcSlots(int pallets) {
        Config config = new Config();
        int minutes = config.timeForPallet() * pallets;
        int x = (int) Math.ceil(minutes / 30);
        return x <= 0 ? 1 : x;
    }

    LocalDateTime get_today_openingtime() {
        var today = LocalDate.now();
        return LocalDateTime.of(today.getYear(), today.getMonth(), today.getDayOfMonth(), 8, 0);
    }

    LocalDateTime findNextAvailableSlot(int slots, boolean loading) throws IOException {

        loadAllResevation();

//        System.out.println("V----------------------------------V");

        Config config = new Config();
        var from = get_today_openingtime();
        var to = from.plus(Duration.ofMinutes(30*slots));// + std::chrono::minutes(slots * 30);
        while (true) {
            if (loading) {
                boolean intersect = false;
                for (var reservation : this._loading) {

//                    System.out.println(String.format("Reservation: %s - %s - duration: %s", reservation.start_at, reservation.end_at, Duration.between(reservation.start_at, reservation.end_at)));
//                    System.out.println(String.format("search: %s - %s - duration: %s", from, to, Duration.between(reservation.start_at, reservation.end_at)));

                    if (!from.isBefore(reservation.start_at) && from.isBefore(reservation.end_at)) {
                        intersect = true;
                    }
                    if (!to.isBefore(reservation.start_at) && to.isBefore(reservation.end_at)) {
                        intersect = true;
                    }
                }
                if (!intersect) {
//                    System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                    return from;
                }
                // increments of 1 slots
                from = from.plus(Duration.ofMinutes(30));
                to = to.plus(Duration.ofMinutes(30));
            } else {
                boolean intersect = false;
                for (var reservation : this._unloading) {
                    if (!from.isBefore(reservation.start_at) && from.isBefore(reservation.end_at)) {
                        intersect = true;
                    }
                    if (!to.isBefore(reservation.start_at) && to.isBefore(reservation.end_at)) {
                        intersect = true;
                    }
                }
                if (!intersect) {
                    return from;
                }
                // increments of 1 slots
                from = from.plus(Duration.ofMinutes(30));
                to = to.plus(Duration.ofMinutes(30));
            }
        }
    }

    void loadAllResevation() throws IOException {
        {
            _loading.clear();
            _unloading.clear();
            Config config = new Config();
            var folder = new File(config.reservation_folder());
            FileUtils.forceMkdir(folder);
            for (var x : folder.listFiles(File::isDirectory)) {
                var reservation = read_reservation(x.toPath());
                if (reservation.type.equals("loading")) _loading.add(reservation);
                else _unloading.add(reservation);
            }
        }
    }

    int findNextId() {
        int id = 1;
        for (var r : this._loading) {
            if (r.id >= id) {
                id = r.id + 1;
            }
        }
        for (var r : this._unloading) {
            if (r.id >= id) {
                id = r.id + 1;
            }
        }
        return id;
    }

    String format(LocalDateTime dt) {
        return dt.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    int reserveSlot(LocalDateTime start_at, int slots, boolean loading) throws IOException {
        loadAllResevation();

        var end_at = start_at;
        end_at = end_at.plusMinutes(slots * 30);

        if (loading) {
            for (var reservation : this._loading) {
                if (!start_at.isBefore(reservation.start_at) && start_at.isBefore(reservation.end_at)) {
                    return -1;
                }
                if (!end_at.isBefore(reservation.start_at) && end_at.isBefore(reservation.end_at)) {
                    return -1;
                }
            }
            int newId = findNextId();

            Config config = new Config();
            var folder = Paths.get(config.reservation_folder(), String.valueOf(newId));
            FileUtils.forceMkdir(folder.toFile());

            var info = new ReservationInfo(newId, loading ? "loading" : "unloading", format(start_at), format(end_at));
            Gson gson = new Gson();
            var filePath = Paths.get(folder.toString(), "info.json");
            FileUtils.writeStringToFile(filePath.toFile(), gson.toJson(info), "UTF-8");
            return newId;
        } else {
            for (var reservation : this._unloading) {
                if (!start_at.isBefore(reservation.start_at) && start_at.isBefore(reservation.end_at)) {
                    return -1;
                }
                if (!end_at.isBefore(reservation.start_at) && end_at.isBefore(reservation.end_at)) {
                    return -1;
                }
            }
            int newId = findNextId();

            Config config = new Config();
            var folder = Paths.get(config.reservation_folder(), String.valueOf(newId));
            FileUtils.forceMkdir(folder.toFile());

            var info = new ReservationInfo(newId, loading ? "loading" : "unloading", format(start_at), format(end_at));
            Gson gson = new Gson();
            var filePath = Paths.get(folder.toString(), "info.json");
            FileUtils.writeStringToFile(filePath.toFile(), gson.toJson(info), "UTF-8");
            return newId;
        }
    }

    Reservation findReservation(int id) throws IOException {
        Config config = new Config();
        var folder = new File(config.reservation_folder());
        folder.mkdirs();
        for (var x : folder.listFiles(File::isDirectory)) {
            var reservation = read_reservation(x.toPath());
            if (reservation.id == id) {
                return reservation;
            }
        }

        Reservation r = new Reservation();
        r.id = -1;
        return r;
    }

    void append_plate_info(int id, String veichle_plate) throws IOException {
        Config config = new Config();
        var folder = Path.of(config.reservation_folder(), String.valueOf(id));
        var reservation = read_reservation(folder);

        reservation.veichle_plate = veichle_plate;

        var info = new ReservationInfo(reservation.id, reservation.type, format(reservation.start_at), format(reservation.end_at), reservation.first_name, reservation.first_name, reservation.veichle_plate);
        Gson gson = new Gson();
        var filePath = Paths.get(folder.toString(), "info.json");
        FileUtils.writeStringToFile(filePath.toFile(), gson.toJson(info), "UTF-8");
    }

    void append_driver_info(int id, String first_name, String last_name, byte[] document) throws IOException {
        Config config = new Config();
        var reservation_folder = Path.of(config.reservation_folder(), String.valueOf(id));
        var reservation = read_reservation(reservation_folder);

        reservation.first_name = first_name;
        reservation.last_name = last_name;

        var info = new ReservationInfo(reservation.id, reservation.type, format(reservation.start_at), format(reservation.end_at), reservation.first_name, reservation.last_name, reservation.veichle_plate);
        Gson gson = new Gson();
        var filePath = Paths.get(reservation_folder.toString(), "info.json");
        FileUtils.writeStringToFile(filePath.toFile(), gson.toJson(info), "UTF-8");

        var document_path = Path.of(reservation_folder.toString(), "document.pdf");
        FileUtils.writeByteArrayToFile(document_path.toFile(), document);
    }

    void archive_reservation() {
        // TODO - move all the reservation of yestrday in a different folder
    }

    LocalDateTime parse(String str) {
        return LocalDateTime.parse(str);
    }

    byte[] read_document(Path folder) throws IOException {
        File document = new File(folder.toString(), "document.pdf");
        if (!document.exists()) {
            return new byte[0];
        }
        return FileUtils.readFileToByteArray(document);
    }

    Reservation read_reservation(Path folder) throws IOException {
        var content = FileUtils.readFileToString(new File(folder.toString(), "info.json"), "UTF-8");

        Reservation r = new Reservation();
        Gson gson = new Gson();
        var info = gson.fromJson(content, ReservationInfo.class);

        r.id = info.id;
        r.type = info.type;
        r.start_at = parse(info.start_at);
        r.end_at = parse(info.end_at);
        r.first_name = info.first_name != null ? info.first_name : "";
        r.last_name = info.last_name != null ? info.last_name : "";
        r.veichle_plate = info.veichle_plate != null ? info.veichle_plate : "";
        r.document = read_document(folder);
        return r;
    }

    List<Reservation> list_all_reservation_of(int year, int month, int day) throws IOException {
        Config config = new Config();
        var folder = new File(config.reservation_folder());
        FileUtils.forceMkdir(folder);
        List<Reservation> ret = new ArrayList<>();
        for (var x : folder.listFiles(File::isDirectory)) {
            var reservation = read_reservation(x.toPath());
            var y = reservation.start_at.getYear();
            var m = reservation.start_at.getMonth().getValue();
            var d = reservation.start_at.getDayOfMonth();
            if (y == year && m == month && day == day) {
                ret.add(reservation);
            }
        }
        return ret;
    }

    boolean isValidVeichelPlate(String plate) {
        return plate.length() >= 5 && plate.length() <= 7 && Character.isAlphabetic(plate.charAt(0)) && Character.isAlphabetic(plate.charAt(1));
    }

    void write_in_share_folder(int id, String content) throws IOException {
        Config config = new Config();
        String folder = config.shared_folder_for_email();
        FileUtils.forceMkdir(new File(folder));

        String name;
        if (folder.endsWith("/")) {
            name = folder;
            name += "email_for_";
            name += String.valueOf(id);
            name += ".eml";
        } else {
            name = folder;
            name += "/";
            name += "email_for_";
            name += String.valueOf(id);
            name += ".eml";
        }
        FileUtils.writeStringToFile(new File(name), content, "UTF-8");
    }

    void check_missing_info() throws IOException {
        List<Reservation> ret = new ArrayList<>();
        var y = LocalDateTime.now().getYear();
        var m = LocalDateTime.now().getMonth();
        var d = LocalDateTime.now().getDayOfMonth();
        var reservations = list_all_reservation_of(y, m.getValue(), d);
        for (var reservation : reservations) {
            if (reservation.document.length == 0) {
                ret.add(reservation);
            } else if (reservation.first_name.isEmpty()) {
                ret.add(reservation);
            } else if (reservation.last_name.isEmpty()) {
                ret.add(reservation);
            } else if (reservation.veichle_plate.isEmpty()) {
                ret.add(reservation);
            } else if (isValidVeichelPlate(reservation.veichle_plate) == false) {
                ret.add(reservation);
            }
        }

        for (var reservation : ret) {
            if (reservation.id > 0) {
                StringBuffer sb = new StringBuffer();
                sb.append("The reservation with id: ").append(reservation.id).append(" hasn't the required documents: ").append("\n");
                if (reservation.document.length == 0) {
                    sb.append("- document is missing").append("\n");
                }
                if (reservation.first_name.isEmpty() || reservation.last_name.isEmpty()) {
                    sb.append("- The name of driver is missing").append("\n");
                }
                if (reservation.veichle_plate.isEmpty()) {
                    sb.append("- The plate is missing").append("\n");
                } else if (isValidVeichelPlate(reservation.veichle_plate) == false) {
                    sb.append("- The plate '").append(reservation.veichle_plate).append("' is not valid").append("\n");
                }

                write_in_share_folder(reservation.id, sb.toString());
            }
        }
    }

    private class ReservationInfo {
        private final int id;
        private final String type;
        private final String start_at;
        private final String end_at;
        private final String first_name;
        private final String last_name;
        private final String veichle_plate;

        public ReservationInfo(int id, String type, String start_at, String end_at, String first_name, String last_name, String veichle_plate) {
            this.id = id;
            this.type = type;
            this.start_at = start_at;
            this.end_at = end_at;
            this.first_name = first_name;
            this.last_name = last_name;
            this.veichle_plate = veichle_plate;
        }

        public ReservationInfo(int id, String type, String start_at, String end_at) {
            this(id, type, start_at, end_at, null, null, null);
        }

    }
}
