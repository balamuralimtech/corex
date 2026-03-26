package com.web.shipx.menu;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import com.web.coretix.menu.AppMenuGroup;
import com.web.coretix.menu.AppMenuItem;
import com.web.coretix.menu.MenuContributor;

@ApplicationScoped
public class ShipxMenuContributor implements MenuContributor {

    @Override
    public List<AppMenuGroup> contribute() {
        AppMenuGroup shipmentOps = new AppMenuGroup("shipx_shipment_ops", "Shipment Ops", "pi pi-fw pi-send",
                100, "true")
                .addItem(new AppMenuItem("shipx_shipment_dashboard", "Shipment Dashboard", "pi pi-fw pi-home",
                        "/pages/shipx/shipment-ops/shipment-dashboard.xhtml", 10, "true"))
                .addItem(new AppMenuItem("shipx_load_planning", "Load Planning", "pi pi-fw pi-directions",
                        "/pages/shipx/shipment-ops/load-planning.xhtml", 20, "true"))
                .addItem(new AppMenuItem("shipx_route_board", "Route Board", "pi pi-fw pi-map",
                        "/pages/shipx/shipment-ops/route-board.xhtml", 30, "true"));

        AppMenuGroup fleetManagement = new AppMenuGroup("shipx_fleet_management", "Fleet Management",
                "pi pi-fw pi-truck", 110, "true")
                .addItem(new AppMenuItem("shipx_vehicle_registry", "Vehicle Registry", "pi pi-fw pi-truck",
                        "/pages/shipx/fleet-management/vehicle-registry.xhtml", 10, "true"))
                .addItem(new AppMenuItem("shipx_driver_roster", "Driver Roster", "pi pi-fw pi-users",
                        "/pages/shipx/fleet-management/driver-roster.xhtml", 20, "true"))
                .addItem(new AppMenuItem("shipx_maintenance_board", "Maintenance Board", "pi pi-fw pi-wrench",
                        "/pages/shipx/fleet-management/maintenance-board.xhtml", 30, "true"));

        AppMenuGroup deliveryHub = new AppMenuGroup("shipx_delivery_hub", "Delivery Hub", "pi pi-fw pi-box",
                120, "true")
                .addItem(new AppMenuItem("shipx_delivery_queue", "Delivery Queue", "pi pi-fw pi-list",
                        "/pages/shipx/delivery-hub/delivery-queue.xhtml", 10, "true"))
                .addItem(new AppMenuItem("shipx_proof_of_delivery", "Proof Of Delivery", "pi pi-fw pi-check-square",
                        "/pages/shipx/delivery-hub/proof-of-delivery.xhtml", 20, "true"))
                .addItem(new AppMenuItem("shipx_exception_desk", "Exception Desk", "pi pi-fw pi-exclamation-triangle",
                        "/pages/shipx/delivery-hub/exception-desk.xhtml", 30, "true"));

        return Arrays.asList(shipmentOps, fleetManagement, deliveryHub);
    }
}
