package com.drb.DrbMVP.service;

import com.drb.DrbMVP.repository.TransportRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransportService {

    private final TransportRepository transportRepository;

    public TransportService(TransportRepository transportRepository) {
        this.transportRepository = transportRepository;
    }

    public List<Map<String, Object>> getNearestStops(double lat, double lon) {
        return transportRepository.findNearestStops(lat, lon);
    }

    public Map<String, Object> getStopWithDepartures(String stopId) {
        Map<String, Object> result = new HashMap<>();
        result.put("stop", transportRepository.findStopDetails(stopId));
        result.put("next_departures", transportRepository.findNextDepartures(stopId));
        return result;
    }

    public Map<String, Object> getRouteWithNearestStop(String routeShort, double lat, double lon) {
        Map<String, Object> result = new HashMap<>();
        result.put("route_stops", transportRepository.findRouteGeoJson(routeShort));
        result.put("nearest_stop", transportRepository.findNearestStopOfRoute(routeShort, lat, lon));
        return result;
    }
}