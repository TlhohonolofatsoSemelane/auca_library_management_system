package com.auca.library.service;

import com.auca.library.domain.Location;
import com.auca.library.util.HibernateUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import java.util.UUID;
import static org.junit.Assert.*;

public class LocationServiceTest {
    private LocationService locationService;

    @Before
    public void setUp() {
        locationService = new LocationService();
    }

    @AfterClass
    public static void tearDown() {
        HibernateUtil.shutdown();
    }

    @Test
    public void createProvince_withNoParent_succeeds() {
        Location province = new Location("Kigali", "KGL", Location.LocationType.PROVINCE);
        Location saved = locationService.createLocation(province, null);
        assertNotNull(saved.getId());
    }

    @Test
    public void createDistrict_withValidProvinceParent_succeeds() {
        Location province = new Location("Northern Province", "NP", Location.LocationType.PROVINCE);
        locationService.createLocation(province, null);

        Location district = new Location("Musanze", "MSZ", Location.LocationType.DISTRICT);
        Location savedDistrict = locationService.createLocation(district, province.getId());

        assertNotNull(savedDistrict.getId());
        assertEquals(province.getId(), savedDistrict.getParent().getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createDistrict_withMissingParent_throwsException() {
        Location district = new Location("Gasabo", "GSB", Location.LocationType.DISTRICT);
        locationService.createLocation(district, UUID.randomUUID()); // Random non-existent parent ID
    }

    @Test(expected = IllegalArgumentException.class)
    public void createLocation_duplicateLocationCode_throwsException() {
        Location province1 = new Location("Eastern Province", "EP", Location.LocationType.PROVINCE);
        locationService.createLocation(province1, null);

        Location province2 = new Location("Duplicate East", "EP", Location.LocationType.PROVINCE);
        locationService.createLocation(province2, null); // Should throw exception
    }

    @Test
    public void validVillageId_returnsCorrectProvinceName() {
        Location province = locationService.createLocation(new Location("Western Province", "WP", Location.LocationType.PROVINCE), null);
        Location district = locationService.createLocation(new Location("Rubavu", "RBV", Location.LocationType.DISTRICT), province.getId());
        Location sector = locationService.createLocation(new Location("Gisenyi", "GSY", Location.LocationType.SECTOR), district.getId());
        Location cell = locationService.createLocation(new Location("Mbugangari", "MBG", Location.LocationType.CELL), sector.getId());
        Location village = locationService.createLocation(new Location("Amahoro", "AMH", Location.LocationType.VILLAGE), cell.getId());

        String provinceName = locationService.getProvinceNameByVillageId(village.getId());
        assertEquals("Western Province", provinceName);
    }
}