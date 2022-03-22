import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;


class DockPlannerTest {

    DockPlanner planner = new DockPlanner();

    @BeforeEach
    void setUp() throws IOException {
        Config config = new Config();
        FileUtils.deleteDirectory(new File(config.reservation_folder()));
        FileUtils.deleteDirectory(new File(config.shared_folder_for_email()));
    }

    @Test
    public void shouldReserveSlotsForLoadingSomePallets() throws IOException {
        var slots = planner.calcSlots(3);
        var from = planner.findNextAvailableSlot(slots, true);

        var res = planner.reserveSlot(from, slots, true);

        assertTrue(res != 0);
    }

    @Test
    public void shouldReserveSlotsForUnloadingSomePallets() throws IOException {
        var slots = planner.calcSlots(3);
        var from = planner.findNextAvailableSlot(slots, false);

        var res = planner.reserveSlot(from, slots, false);

        assertTrue(res != 0);
    }

    @Test
    public void shouldReturnSameAvailableSlotIfNoReserveSlotIsCalled() throws IOException {
        var slots = planner.calcSlots(3);
        var loadFrom = planner.findNextAvailableSlot(slots, true);
        var loadFrom1 = planner.findNextAvailableSlot(slots, true);
        var unloadingFrom = planner.findNextAvailableSlot(slots, false);
        var unloadingFrom1 = planner.findNextAvailableSlot(slots, false);

        assertEquals(loadFrom, loadFrom1);
        assertEquals(unloadingFrom, unloadingFrom1);
    }

    @Test
    public void shouldReturnDifferentSlotAfterAReservation() throws IOException {
        var slots = planner.calcSlots(3);
        var loadFrom = planner.findNextAvailableSlot(slots, true);

        planner.reserveSlot(loadFrom, slots, true);

        var loadFrom1 = planner.findNextAvailableSlot(slots, true);

        assertNotEquals(loadFrom, loadFrom1);
    }

    @Test
    public void shouldStartFromSameTimeForUnlaodingAndLoading() throws IOException {
        var slots = planner.calcSlots(3);
        var loadFrom = planner.findNextAvailableSlot(slots, true);
        var unloadingFrom = planner.findNextAvailableSlot(slots, false);

        assertEquals(loadFrom, unloadingFrom);
    }

    @Test
    public void shouldKeepLoadingAndUnloadingOperationSeparated() throws IOException {
        var slots = planner.calcSlots(3);
        var fromLoading = planner.findNextAvailableSlot(slots, true);
        var fromUnoading = planner.findNextAvailableSlot(slots, false);

        var res = planner.reserveSlot(fromLoading, slots, false);

        var fromLoading1 = planner.findNextAvailableSlot(slots, true);
        var fromUnoading1 = planner.findNextAvailableSlot(slots, false);

        assertEquals(fromLoading, fromLoading1);
        assertNotEquals(fromUnoading, fromUnoading1);
    }

    @Test
    public void shouldReturnIncreasingAvailableSlots() throws IOException {
        var slots = planner.calcSlots(3);
        var f1 = planner.findNextAvailableSlot(slots, true);
        assertTrue(planner.reserveSlot(f1, slots, true) > 0);

        var f2 = planner.findNextAvailableSlot(slots, true);
        assertTrue(planner.reserveSlot(f2, slots, true) > 0);

        var f3 = planner.findNextAvailableSlot(slots, true);
        assertTrue(planner.reserveSlot(f3, slots, true) > 0);

        var f4 = planner.findNextAvailableSlot(slots, true);
        assertTrue(planner.reserveSlot(f4, slots, true) > 0);
        assertTrue(f1.isBefore(f2));
        var xxx = Duration.between(f1, f2);

        assertEquals(xxx, Duration.ofMinutes(slots * 30));

        assertTrue(f2.isBefore(f3));
        assertTrue(f3.isBefore(f4));
        assertEquals(Duration.between(f3, f4), Duration.ofMinutes(slots * 30));
    }

    @Test
    public void shouldReserveSlotsAtTimePassed() throws IOException {
        var slots = planner.calcSlots(3);
        planner.findNextAvailableSlot(slots, true);
        var from = planner.findNextAvailableSlot(slots, true);
        var id = planner.reserveSlot(from, 3, true);

        Config config = new Config();
        var path = Paths.get(config.reservation_folder(), String.valueOf(id));
        var reservation = planner.read_reservation(path);

        assertEquals(reservation.start_at, from);

        assertEquals(reservation.end_at, from.plus(Duration.ofMinutes(slots * 30)));
    }

    @Test
    public void shouldUpdateDriverData() throws IOException {
        var slots = planner.calcSlots(3);
        planner.findNextAvailableSlot(slots, true);
        var from = planner.findNextAvailableSlot(slots, true);
        var id = planner.reserveSlot(from, 3, true);

        var document = FileUtils.readFileToByteArray(new File("src/test/resources/jon_snow.pdf"));
        assertTrue(document.length > 0);

        planner.append_driver_info(id, "Jon", "Snow", document);

        Config config = new Config();
        var path = Paths.get(config.reservation_folder(), String.valueOf(id));
        var reservation = planner.read_reservation(path);

        assertEquals(reservation.first_name, "Jon");
        assertEquals(reservation.last_name, "Snow");
        assertArrayEquals(reservation.document, document);
    }

    @Test
    public void shouldUpdateVeichlePlate() throws IOException {
        var slots = planner.calcSlots(3);
        planner.findNextAvailableSlot(slots, true);
        var from = planner.findNextAvailableSlot(slots, true);
        var id = planner.reserveSlot(from, 3, true);

        var document = FileUtils.readFileToByteArray(new File("src/test/resources/jon_snow.pdf"));
        assertTrue(document.length > 0);

        planner.append_driver_info(id, "Jon", "Snow", document);
        planner.append_plate_info(id, "1234");
        planner.append_plate_info(id, "XX666XX");

        Config config = new Config();
        var path = Paths.get(config.reservation_folder(), String.valueOf(id));
        var reservation = planner.read_reservation(path);

        assertEquals(reservation.first_name, "Jon");
        assertEquals(reservation.last_name, "Snow");
        assertArrayEquals(reservation.document, document);
        assertEquals(reservation.veichle_plate, "XX666XX");
    }

    @Test
    public void shouldSendEmailForReservationWithoutDriverInfo() throws IOException {
        var slots = planner.calcSlots(3);
        planner.reserveSlot(planner.findNextAvailableSlot(slots, true), 3, true);
        var id = planner.reserveSlot(planner.findNextAvailableSlot(slots, true), 3, true);
        var document = FileUtils.readFileToByteArray(new File("src/test/resources/jon_snow.pdf"));
        assertTrue(document.length > 0);

        planner.append_driver_info(id, "Jon", "Snow", document);
        planner.append_plate_info(id, "XX666XX");

        planner.check_missing_info();

        Config config = new Config();
        var folder = new File(config.shared_folder_for_email());
        for (var x : folder.listFiles(File::isFile)) {
            var str = FileUtils.readFileToString(x, "UTF-8");
            assertEquals(str, "The reservation with id: 1 hasn't the required documents: \n- document is missing\n- The name of driver is missing\n- The plate is missing\n");
        }
    }

    @Test
    public void shouldSendEmailForReservationWithoutVeichelePlate() throws IOException {
        var slots = planner.calcSlots(3);
        var id = planner.reserveSlot(planner.findNextAvailableSlot(slots, true), 3, true);
        var document = FileUtils.readFileToByteArray(new File("src/test/resources/cersei_lannister.pdf"));
        assertTrue(document.length > 0);
        planner.append_driver_info(id, "Cersei", "Lannister", document);

        id = planner.reserveSlot(planner.findNextAvailableSlot(slots, true), 3, true);
        document = FileUtils.readFileToByteArray(new File("src/test/resources/jon_snow.pdf"));
        assertTrue(document.length > 0);
        planner.append_driver_info(id, "Jon", "Snow", document);
        planner.append_plate_info(id, "XX666XX");

        planner.check_missing_info();

        Config config = new Config();
        var folder = new File(config.shared_folder_for_email());
        for (var x : folder.listFiles(File::isFile)) {
            var str = FileUtils.readFileToString(x, "UTF-8");
            assertEquals(str, "The reservation with id: 1 hasn't the required documents: \n- The plate is missing\n");
        }
    }

    @Test
    public void shouldSendEmailForReservationWithInvalidVeichelePlate() throws IOException {
        var slots = planner.calcSlots(3);
        var id = planner.reserveSlot(planner.findNextAvailableSlot(slots, true), 3, true);
        var document = FileUtils.readFileToByteArray(new File("src/test/resources/cersei_lannister.pdf"));
        assertTrue(document.length > 0);
        planner.append_driver_info(id, "Cersei", "Lannister", document);
        planner.append_plate_info(id, "cersei rules");

        id = planner.reserveSlot(planner.findNextAvailableSlot(slots, true), 3, true);
        document = FileUtils.readFileToByteArray(new File("src/test/resources/jon_snow.pdf"));
        assertTrue(document.length > 0);
        planner.append_driver_info(id, "Jon", "Snow", document);
        planner.append_plate_info(id, "XX666XX");

        planner.check_missing_info();

        Config config = new Config();
        var folder = new File(config.shared_folder_for_email());
        for (var x : folder.listFiles(File::isFile)) {
            var str = FileUtils.readFileToString(x, "UTF-8");
            assertEquals(str, "The reservation with id: 1 hasn't the required documents: \n- The plate 'cersei rules' is not valid\n");
        }
    }

//    planner.check_missing_info
//     TODO -- planner.archive_reservation
//    @Test
//    public void shouldMoveOldReservationFromReservationFolderToArchiveFolder()
//    {
//        FAIL("TODO");
//    }
}