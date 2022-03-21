import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputFilter;

import static org.junit.jupiter.api.Assertions.*;


class DockPlannerTest {

    @BeforeEach
    void setUp() throws IOException {
        ObjectInputFilter.Config config;
        FileUtils.deleteDirectory(new File("directory"));
    }

    DockPlanner planner = new DockPlanner();

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

//    @Test
//    public void shouldreturnSameAvailableSlotIfNoReserveSlotIsCalled() {
//        DockPlanner planner;
//        var slots = planner.calcSlots(3);
//        var loadFrom = planner.findNextAvailableSlot(slots, true);
//        var loadFrom1 = planner.findNextAvailableSlot(slots, true);
//        var unloadingFrom = planner.findNextAvailableSlot(slots, false);
//        var unloadingFrom1 = planner.findNextAvailableSlot(slots, false);
//
//        assertEquals(loadFrom, loadFrom1);
//        assertEquals(unloadingFrom, unloadingFrom1);
//    }
//
//    @Test
//    public void shouldReturnDifferentSlotAfterAReservation() {
//        DockPlanner planner;
//        var slots = planner.calcSlots(3);
//        var loadFrom = planner.findNextAvailableSlot(slots, true);
//
//        planner.reserveSlot(loadFrom, slots, true);
//
//        var loadFrom1 = planner.findNextAvailableSlot(slots, true);
//
//        assertNotEquals(loadFrom, loadFrom1);
//    }
//
//    @Test
//    public void shouldStartFromSameTimeForUnlaodingAndLoading() {
//        DockPlanner planner;
//        var slots = planner.calcSlots(3);
//        var loadFrom = planner.findNextAvailableSlot(slots, true);
//        var unloadingFrom = planner.findNextAvailableSlot(slots, false);
//
//        assertEquals(loadFrom, unloadingFrom);
//    }
//
//    @Test
//    public void shouldKeepLoadingAndUnloadingOperationSeparated() {
//        DockPlanner planner;
//        var slots = planner.calcSlots(3);
//        var fromLoading = planner.findNextAvailableSlot(slots, true);
//        var fromUnoading = planner.findNextAvailableSlot(slots, false);
//
//        var res = planner.reserveSlot(fromLoading, slots, false);
//
//        var fromLoading1 = planner.findNextAvailableSlot(slots, true);
//        var fromUnoading1 = planner.findNextAvailableSlot(slots, false);
//
//        assertThat(fromLoading, fromLoading1);
//        EXPECT_NE(fromUnoading, fromUnoading1);
//    }
//
//    @Test
//    public void shouldReturnIncreasingAvailableSlots() {
//        DockPlanner planner;
//        var slots = planner.calcSlots(3);
//        std::cout << "slots " << slots << std::endl;
//        var f1 = planner.findNextAvailableSlot(slots, true);
//        EXPECT_TRUE(planner.reserveSlot(f1, slots, true));
//
//        var f2 = planner.findNextAvailableSlot(slots, true);
//        EXPECT_TRUE(planner.reserveSlot(f2, slots, true));
//
//        var f3 = planner.findNextAvailableSlot(slots, true);
//        EXPECT_TRUE(planner.reserveSlot(f3, slots, true));
//
//        var f4 = planner.findNextAvailableSlot(slots, true);
//        EXPECT_TRUE(planner.reserveSlot(f4, slots, true));
//        EXPECT_TRUE(f1 < f2);
//        var xxx = std::chrono::duration_cast < std::chrono::minutes > (f2 - f1);
//
//        assertThat(xxx.count(), std::chrono::minutes (slots * 30).count());
//
//        EXPECT_TRUE(f2 < f3);
//        EXPECT_TRUE(f3 < f4);
//        assertThat((f4 - f3), std::chrono::minutes (slots * 30));
//    }
//
//    @Test
//    public void shouldReserveSlotsAtTimePassed() {
//        var slots = planner.calcSlots(3);
//        planner.findNextAvailableSlot(slots, true);
//        var from = planner.findNextAvailableSlot(slots, true);
//        var id = planner.reserveSlot(from, 3, true);
//
//        Config config;
//        boost::filesystem::path f = config.reservation_folder();
//        f /= std::to_string (id);
//        var reservation = planner.read_reservation(f);
//
//        assertThat(reservation.start_at, from);
//
//        // var elapsed = reservation.end_at - reservation.start_at;
//        // std::cout << "elapsed: " << std::chrono::duration_cast<std::chrono::minutes>(elapsed).count() << std::endl;
//        // var elapsed2 = reservation.end_at - from;
//        // std::cout << "elapsed2: " << std::chrono::duration_cast<std::chrono::minutes>(elapsed2).count() << std::endl;
//        // std::cout << (reservation.start_at == from) << std::endl;
//        assertThat(reservation.end_at, from + std::chrono::minutes (slots * 30));
//    }
//
//    @Test
//    public void shouldUpdateDriverData() {
//        var slots = planner.calcSlots(3);
//        planner.findNextAvailableSlot(slots, true);
//        var from = planner.findNextAvailableSlot(slots, true);
//        var id = planner.reserveSlot(from, 3, true);
//
//        std::ifstream input("jon_snow.pdf", std::ios::binary);
//        var document = std::vector < char>(std::istreambuf_iterator < char>(input), {});
//        ASSERT_GT(document.size(), 0);
//
//        planner.append_driver_info(id, "Jon", "Snow", document);
//
//        Config config;
//        boost::filesystem::path f = config.reservation_folder();
//        f /= std::to_string (id);
//        var reservation = planner.read_reservation(f);
//
//        assertThat(reservation.first_name, "Jon");
//        assertThat(reservation.last_name, "Snow");
//        assertThat(reservation.document, document);
//    }
//
//    @Test
//    public void shouldUpdateVeichlePlate() {
//        var slots = planner.calcSlots(3);
//        planner.findNextAvailableSlot(slots, true);
//        var from = planner.findNextAvailableSlot(slots, true);
//        var id = planner.reserveSlot(from, 3, true);
//
//        std::ifstream input("jon_snow.pdf", std::ios::binary);
//        var document = std::vector < char>(std::istreambuf_iterator < char>(input), {});
//        ASSERT_GT(document.size(), 0);
//
//        planner.append_driver_info(id, "Jon", "Snow", document);
//        planner.append_plate_info(id, "1234");
//        planner.append_plate_info(id, "XX666XX");
//
//        Config config;
//        boost::filesystem::path f = config.reservation_folder();
//        f /= std::to_string (id);
//        var reservation = planner.read_reservation(f);
//
//        assertThat(reservation.first_name, "Jon");
//        assertThat(reservation.last_name, "Snow");
//        assertThat(reservation.document, document);
//    }
//
//    @Test
//    public void shouldSendEmailForReservationWithoutDriverInfo() {
//        var slots = planner.calcSlots(3);
//        planner.reserveSlot(planner.findNextAvailableSlot(slots, true), 3, true);
//        var id = planner.reserveSlot(planner.findNextAvailableSlot(slots, true), 3, true);
//        std::ifstream input("jon_snow.pdf", std::ios::binary);
//        var document = std::vector < char>(std::istreambuf_iterator < char>(input), {});
//        ASSERT_GT(document.size(), 0);
//        planner.append_driver_info(id, "Jon", "Snow", document);
//        planner.append_plate_info(id, "XX666XX");
//
//        planner.check_missing_info();
//
//        Config config;
//        boost::filesystem::path folder = config.shared_folder_for_email();
//        for (directory_entry & x :directory_iterator(folder))
//        {
//            std::ifstream input(x.path().string());
//            std::string str((std::istreambuf_iterator < char>(input)),std::istreambuf_iterator < char>());
//            assertThat(str, "The reservation with id: 1 hasn't the required documents: \n- document is missing\n- The name of driver is missing\n- The plate is missing\n\n");
//        }
//    }
//
//    @Test
//    public void shouldSendEmailForReservationWithoutVeichelePlate() throws IOException {
//
//        var slots = planner.calcSlots(3);
//        var id = planner.reserveSlot(planner.findNextAvailableSlot(slots, true), 3, true);
////        std::ifstream input("cersei_lannister.pdf", std::ios::binary);
////        var document = std::vector<char>(std::istreambuf_iterator<char>(input), {});
//        var document = FileUtils.readFileToByteArray(new File("cersei_lannister.pdf"));
////        ASSERT_GT(document.size(), 0);
//        assertThat(document.length > 0);
//        planner.append_driver_info(id, "Cersei", "Lannister", document);
//
//        id = planner.reserveSlot(planner.findNextAvailableSlot(slots, true), 3, true);
//        std::ifstream input_js("jon_snow.pdf", std::ios::binary);
//        document = std::vector < char>(std::istreambuf_iterator < char>(input_js), {});
//        ASSERT_GT(document.size(), 0);
//        planner.append_driver_info(id, "Jon", "Snow", document);
//        planner.append_plate_info(id, "XX666XX");
//
//        planner.check_missing_info();
//
//        Config config;
//        boost::filesystem::path folder = config.shared_folder_for_email();
//        for (directory_entry & x :directory_iterator(folder))
//        {
//            std::ifstream input(x.path().string());
//            std::string str((std::istreambuf_iterator < char>(input)),std::istreambuf_iterator < char>());
//            assertThat(str, "The reservation with id: 1 hasn't the required documents: \n- The plate is missing\n\n");
//        }
//    }
//
//    @Test
//    public void shouldSendEmailForReservationWithInvalidVeichelePlate() {
//        var slots = planner.calcSlots(3);
//        var id = planner.reserveSlot(planner.findNextAvailableSlot(slots, true), 3, true);
//        std::ifstream input("cersei_lannister.pdf", std::ios::binary);
//        var document = std::vector < char>(std::istreambuf_iterator < char>(input), {});
//        ASSERT_GT(document.size(), 0);
//        planner.append_driver_info(id, "Cersei", "Lannister", document);
//        planner.append_plate_info(id, "cersei rules");
//
//        id = planner.reserveSlot(planner.findNextAvailableSlot(slots, true), 3, true);
//        std::ifstream input_js("jon_snow.pdf", std::ios::binary);
//        document = std::vector < char>(std::istreambuf_iterator < char>(input_js), {});
//        ASSERT_GT(document.size(), 0);
//        planner.append_driver_info(id, "Jon", "Snow", document);
//        planner.append_plate_info(id, "XX666XX");
//
//        planner.check_missing_info();
//
//        Config config;
//        boost::filesystem::path folder = config.shared_folder_for_email();
//        for (directory_entry & x :directory_iterator(folder))
//        {
//            std::ifstream input(x.path().string());
//            std::string str((std::istreambuf_iterator < char>(input)),std::istreambuf_iterator < char>());
//            assertThat(str, "The reservation with id: 1 hasn't the required documents: \n- The plate 'cersei rules' is not valid\n\n");
//        }
//    }
//
//    //planner.check_missing_info
//// TODO -- planner.archive_reservation
////@Test
//    public void shouldMoveOldReservationFromReservationFolderToArchiveFolder)
//
//    {
//        FAIL("TODO");
//    }


}