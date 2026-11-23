package com.reloop.service;

import com.reloop.dao.AuditDAO;
import com.reloop.dao.StationDAO;
import com.reloop.dao.UserDAO;
import com.reloop.exception.DatabaseException;
import com.reloop.model.StationSummary;
import com.reloop.model.User;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service providing high-level operational statistics for the Admin dashboard.
 */
public class AdminService {

    private final AuditDAO auditDAO = new AuditDAO();
    private final StationDAO stationDAO = new StationDAO();
    private final UserDAO userDAO = new UserDAO();

    public Map<String, Object> getAdminDashboardData() throws DatabaseException {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("metrics", auditDAO.getSystemOverview());
        data.put("stations", stationDAO.getStationSummaries());
        data.put("users", userDAO.findAll());
        return data;
    }
}
