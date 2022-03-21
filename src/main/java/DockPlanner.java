import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
};

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
};

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
        //      var tp = std::chrono::system_clock::now ();
//        LocalDateTime.now()
        var today = LocalDate.now();
//        LocalTime.of(8,0);
        return LocalDateTime.of(today.getYear(), today.getMonth(), today.getDayOfMonth(), 8, 0);
//        std::time_t tt = std::chrono::system_clock::to_time_t (tp);
//        tm utc_tm;
//        gmtime_r( & tt, &utc_tm);
//        utc_tm.tm_hour = 8;
//        utc_tm.tm_min = 0;
//        utc_tm.tm_sec = 0;
//        return std::chrono::system_clock::from_time_t (std::mktime ( & utc_tm));
    }

    LocalDateTime findNextAvailableSlot(int slots, boolean loading) throws IOException {

        loadAllResevation();

        //std::cout << "V----------------------------------V" << std::endl;

        Config config = new Config();
        var from = get_today_openingtime();
        var to = from.plus(Duration.ofMinutes(30));// + std::chrono::minutes(slots * 30);
        while (true) {
            if (loading) {
                boolean intersect = false;
                for (var reservation : this._loading) {

                    //var d = reservation.end_at - reservation.start_at;
                    //var mins = std::chrono::duration_cast<std::chrono::minutes>(d);
                    //std::cout << "reservation: " << format(reservation.start_at) << " - " << format(reservation.end_at) << " - duration: " << mins.count() << std::endl;

                    //var d1 = reservation.end_at - reservation.start_at;
                    //var mins1 = std::chrono::duration_cast<std::chrono::minutes>(d1);
                    //std::cout << " search: " << format(from) << " - " << format(to) << " duration: " << mins1.count() << std::endl;

                    if (!from.isBefore(reservation.start_at) && from.isBefore(reservation.end_at)) {
                        intersect = true;
                    }
                    if (!to.isBefore(reservation.start_at) && to.isBefore(reservation.end_at)) {
                        intersect = true;
                    }
                }
                if (!intersect) {
                    //std::cout << "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^" << std::endl;
                    return from;
                }
                // increments of 1 slots
//                from += std::chrono::minutes(30);
//                to += std::chrono::minutes(30);
                from = from.plus(Duration.ofMinutes(30));
                to = to.plus(Duration.ofMinutes(30));
            } else {
                boolean intersect = false;
                for (var reservation : this._unloading) {
//                    if (from >= reservation.start_at && from < reservation.end_at) {
//                        intersect = true;
//                    }
//                    if (to >= reservation.start_at && to < reservation.end_at) {
//                        intersect = true;
//                    }
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
//                from += std::chrono::minutes(30);
//                to += std::chrono::minutes(30);
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
            //boost::filesystem::create_directory(folder);
            for (var x : folder.listFiles(File::isDirectory))
            {
                var reservation = read_reservation(x.toPath());
                if (reservation.type == "loading")
                    _loading.add(reservation);
                else
                    _unloading.add(reservation);
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

    //    string format(std::chrono::system_clock::time_point tp) {
//        std::time_t tt = std::chrono::system_clock::to_time_t (tp);
//        // tm utc_tm;
//        // gmtime_r(&tt, &utc_tm);
//        tm local_tm;
//        localtime_r( & tt, &local_tm);
//        char mbstr[ 255];
//        // std::strftime(mbstr, sizeof(mbstr), "%Y-%m-%d %H:%M:%S", &utc_tm);
//        std::strftime (mbstr, sizeof(mbstr), "%Y-%m-%d %H:%M:%S", &local_tm);
//        return mbstr;
//    }
    String format(LocalDateTime dt) {
        return dt.format(DateTimeFormatter.ISO_DATE);
//        std::time_t tt = std::chrono::system_clock::to_time_t (tp);
//        // tm utc_tm;
//        // gmtime_r(&tt, &utc_tm);
//        tm local_tm;
//        localtime_r( & tt, &local_tm);
//        char mbstr[ 255];
//        // std::strftime(mbstr, sizeof(mbstr), "%Y-%m-%d %H:%M:%S", &utc_tm);
//        std::strftime (mbstr, sizeof(mbstr), "%Y-%m-%d %H:%M:%S", &local_tm);
//        return mbstr;
    }

    int reserveSlot(LocalDateTime start_at, int slots, boolean loading) throws IOException {
        loadAllResevation();

        var end_at = start_at;
//        end_at += std::chrono::minutes(slots * 30);
        end_at = end_at.plusMinutes(slots * 30);

        if (loading) {
            for (var reservation : this._loading) {
//                if (start_at >= reservation.start_at && start_at < reservation.end_at)
//                    return -1;
//                if (end_at >= reservation.start_at && end_at < reservation.end_at)
//                    return -1;

                if (!start_at.isBefore(reservation.start_at) && start_at.isBefore(reservation.end_at)) {
                    return -1;
                }
                if (!end_at.isBefore(reservation.start_at) && end_at.isBefore(reservation.end_at)) {
                    return -1;
                }

            }
            int newId = findNextId();

            Config config = new Config();
//            boost::filesystem::path f = config.reservation_folder();
//            f /= std::to_string (newId);
//            boost::filesystem::create_directory(f);
            //("foo", "bar", "baz.txt");
            var folderPath = Paths.get(config.reservation_folder(), String.valueOf(newId));
            new File(folderPath.toString()).mkdirs();

            var filePath = Paths.get(String.valueOf(folderPath), "info.json");

            // write prettified JSON to another file
            // TODO!!!
//            value j = json {
//                {
//                    "id", newId
//                },{
//                    "type", loading ? "loading" : "unloading"
//                },{
//                    "start_at", format(start_at)
//                },{
//                    "end_at", format(end_at)
//                }
//            } ;
//            std::ofstream o(f.string());
//            o << std::setw (4) << j << std::endl;
            return newId;
        } else {
            for (var reservation : this._unloading) {
//                if (start_at >= reservation.start_at && start_at < reservation.end_at)
//                    return -1;
//                if (end_at >= reservation.start_at && end_at < reservation.end_at)
//                    return -1;
                if (!start_at.isBefore(reservation.start_at) && start_at.isBefore(reservation.end_at)) {
                    return -1;
                }
                if (!end_at.isBefore(reservation.start_at) && end_at.isBefore(reservation.end_at)) {
                    return -1;
                }
            }
            int newId = findNextId();

            Config config = new Config();
//            boost::filesystem::path f = config.reservation_folder();
//            f /= std::to_string (newId);
//            boost::filesystem::create_directory(f);
//
//            f /= "info.json";
            var folderPath = Paths.get(config.reservation_folder(), String.valueOf(newId));
            new File(folderPath.toString()).mkdirs();

            var filePath = Paths.get(String.valueOf(folderPath), "info.json");

            // write prettified JSON to another file
            //TODO!!!
//            value j = json {
//                {
//                    "id", newId
//                },{
//                    "type", loading ? "loading" : "unloading"
//                },{
//                    "start_at", format(start_at)
//                },{
//                    "end_at", format(end_at)
//                }
//            } ;
//            std::ofstream o(f.string());
//            o << std::setw (4) << j << std::endl;
            return newId;
        }
    }

    Reservation findReservation(int id) throws IOException {
        Config config = new Config();
//        boost::filesystem::path folder = config.reservation_folder();
//        using namespace boost::filesystem;
//        boost::filesystem::create_directory(folder);
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

        //f /= "info.json";

        // write prettified JSON to another file
        // TODO!!
//        value j = json {
//            {
//                "id", reservation.id
//            },
//            {
//                "first_name", reservation.first_name
//            },
//            {
//                "last_name", reservation.last_name
//            },
//            {
//                "type", reservation.type
//            },
//            {
//                "veichle_plate", reservation.veichle_plate
//            },
//            {
//                "start_at", format(reservation.start_at)
//            },
//            {
//                "end_at", format(reservation.end_at)
//            }
//        } ;
//        std::ofstream o(f.string());
//        o << std::setw (4) << j << std::endl;
    }

    void append_driver_info(int id, String first_name, String last_name, byte[] document) throws IOException {
        Config config = new Config();
//        boost::filesystem::path reservatin_folder = config.reservation_folder();
//        reservatin_folder /= std::to_string (id);
        var reservation_folder = Path.of(config.reservation_folder(), String.valueOf(id));
        var reservation = read_reservation(reservation_folder);

        reservation.first_name = first_name;
        reservation.last_name = last_name;

        // write prettified JSON to another file
        // TODO
//        value j = json {
//            {
//                "id", reservation.id
//            },
//            {
//                "first_name", reservation.first_name
//            },
//            {
//                "last_name", reservation.last_name
//            },
//            {
//                "type", reservation.type
//            },
//            {
//                "veichle_plate", reservation.veichle_plate
//            },
//            {
//                "start_at", format(reservation.start_at)
//            },
//            {
//                "end_at", format(reservation.end_at)
//            }
//        } ;
//        var info_path = reservatin_folder / "info.json";
//        std::ofstream o(info_path.string());
//        o << std::setw (4) << j << std::endl;

        var document_path = Path.of(reservation_folder.toString(), "document.pdf");
        //TODO!!!
//        std::ofstream output(document_path.string(), std::ios::binary);
//
//        std::copy (document.begin(), document.end(), std::ostreambuf_iterator < char>(output));
    }

    void archive_reservation() {
        // TODO - move all the reservation of yestrday in a different folder
    }

    LocalDateTime to_time_point(String str) {
//        std::tm tm = {};
//        Stringstream ss (str);
//        ss >> std::get_time ( & tm, "%Y-%m-%d %H:%M:%S");
//        var ret = std::chrono::system_clock::from_time_t (std::mktime ( & tm)-timezone);
//
//        // std::cout << "ret: " << format(ret) << std::endl;
        return LocalDateTime.parse(str);
    }

    byte[] read_document(Path folder) throws IOException {
        return FileUtils.readFileToByteArray(new File(folder.toString(), "document.pdf"));
    }

    Reservation read_reservation(Path folder) throws IOException {
//        var info = folder / "info.json";
//        std::ifstream i(info.string());
//        String content ((std::istreambuf_iterator < char>(i)),
//        (std::istreambuf_iterator < char>()));
//        object j = parse(content).as_object();
        var content = FileUtils.readFileToString(new File(folder.toString(), "info,json"), "UTF-8");

        // TODO PARSE JSON!!!

        Reservation r = new Reservation();
//        r.id = j["id"].as_int64();
//        r.start_at = to_time_point(j["start_at"].as_string().c_str());
//        r.end_at = to_time_point(j["end_at"].as_string().c_str());
//        r.type = j["type"].as_string();
//
//        if (!j["first_name"].is_null())
//            r.first_name = j["first_name"].as_string().c_str();
//        if (!j["last_name"].is_null())
//            r.last_name = j["last_name"].as_string().c_str();
//        if (!j["veichle_plate"].is_null())
//            r.veichle_plate = j["veichle_plate"].as_string().c_str();

        r.document = read_document(folder);
        return r;
    }

    List<Reservation> list_all_reservation_of(int year, int month, int day) {
        Config config = new Config();
        // TODO
//        boost::filesystem::path folder = config.reservation_folder();
        List<Reservation> ret = new ArrayList<>();
        //for (directory_entry& x : directory_iterator(folder / "loading")) {
        //	var reservation = read_reservation(x.path());
        //	time_t tt = system_clock::to_time_t(reservation.start_at);
        //	tm utc_tm;
        //	gmtime_s(&utc_tm, &tt);
        //	var y = utc_tm.tm_year + 1900;
        //	var m = utc_tm.tm_mon + 1;
        //	var d = utc_tm.tm_mday;
        //	if (y == year && m == month && day == day) {
        //		ret.push_back(reservation);
        //	}
        //}

        //for (directory_entry& x : directory_iterator(folder / "unloading")) {
        //	var reservation = read_reservation(x.path());
        //	time_t tt = system_clock::to_time_t(reservation.start_at);
        //	tm utc_tm;
        //	gmtime_s(&utc_tm, &tt);
        //	var y = utc_tm.tm_year + 1900;
        //	var m = utc_tm.tm_mon + 1;
        //	var d = utc_tm.tm_mday;
        //	if (y == year && m == month && day == day) {
        //		ret.push_back(reservation);
        //	}
        //}

//        boost::filesystem::create_directory(folder);
//        for (directory_entry & x :directory_iterator(folder))
//        {
//            var reservation = read_reservation(x.path());
//            time_t tt = system_clock::to_time_t (reservation.start_at);
//            tm utc_tm;
//            gmtime_r( & tt, &utc_tm);
//            var y = utc_tm.tm_year + 1900;
//            var m = utc_tm.tm_mon + 1;
//            var d = utc_tm.tm_mday;
//            if (y == year && m == month && day == day) {
//                ret.push_back(reservation);
//            }
//        }

        return ret;
    }

    boolean isValidVeichelPlate(String plate) {
        if (plate.length() >= 5 && plate.length() <= 7 && Character.isAlphabetic(plate.charAt(0)) && Character.isAlphabetic(plate.charAt(1)))
            return true;
        else
            return false;
    }

    void write_in_share_folder(int id, String content) throws IOException {
        Config config = new Config();
        String folder = config.shared_folder_for_email();

//        boost::filesystem::create_directory(folder);
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

//        std::ofstream outfile(name);
//        outfile << content << std::endl;
//        outfile.close();
        FileUtils.writeStringToFile(new File(name), content, "UTF-8");
    }

    void check_missing_info() throws IOException {
        List<Reservation> ret = new ArrayList<>();
//        system_clock::time_point now = system_clock::now ();
//        time_t tt = system_clock::to_time_t (now);
//        tm utc_tm;
//        gmtime_r( & tt, &utc_tm);
//        var y = utc_tm.tm_year + 1900;
//        var m = utc_tm.tm_mon + 1;
//        var d = utc_tm.tm_mday;
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

//                Stringstream ss;
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
};
