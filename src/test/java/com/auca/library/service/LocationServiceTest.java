package com.auca.library.service;

import com.auca.library.domain.Location;
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

    private String unique(String prefix) {
        return prefix + "_" + System.nanoTime();
    }

    @Test
    public void createProvince_withNoParent_succeeds() {
        Location province = new Location(unique("Kigali"), unique("KGL"), Location.LocationType.PROVINCE);
        Location saved = locationService.createLocation(province, null);
        assertNotNull(saved.getId());
        assertNull(saved.getParent());
    }

    @Test
    public void createDistrict_withValidProvinceParent_succeeds() {
        Location province = locationService.createLocation(new Location(unique("Kigali"), unique("KGL"), Location.LocationType.PROVINCE), null);
        Location district = new Location(unique("Gasabo"), unique("GSB"), Location.LocationType.DISTRICT);
        Location saved = locationService.createLocation(district, province.getId());
        assertNotNull(saved.getId());
        assertEquals(province.getId(), saved.getParent().getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createDistrict_withMissingParent_throwsException() {
        Location district = new Location(unique("Gasabo"), unique("GSB"), Location.LocationType.DISTRICT);
        locationService.createLocation(district, UUID.randomUUID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createLocation_duplicateLocationCode_throwsException() {
        String code = unique("DUP");
        Location loc1 = new Location("Loc 1", code, Location.LocationType.PROVINCE);
        Location loc2 = new Location("Loc 2", code, Location.LocationType.PROVINCE);

        locationService.createLocation(loc1, null);
        locationService.createLocation(loc2, null);
    }
}