/**
 * BARISHAL CONNECT - GOOGLE APPS SCRIPT BACKEND
 * -------------------------------------------------------------
 * This script serves as the centralized API backend for the Barishal Connect App.
 * It maps incoming GET and POST JSON requests directly to Google Sheets database tabs.
 * 
 * SETUP INSTRUCTIONS:
 * 1. Create a Google Sheet named "Barishal Connect Database".
 * 2. Create the following sheet tabs (case-sensitive):
 *    - Users, Districts, Upazilas, Unions, Roads, RoadTracks, Workers, Jobs, Hospitals, Doctors,
 *      Education, Businesses, Tourism, History, GovOffices, Emergency, Weather, Agriculture,
 *      Crops, MarketPrice, BusSchedules, LaunchSchedules, BloodDonors, CitizenReports,
 *      Notifications, Reviews, Donations, Developer, Settings, Analytics, Logs.
 * 3. Open Extensions > Apps Script in your spreadsheet.
 * 4. Paste this entire code, save, and deploy as a Web App:
 *    - Execute as: "Me (your-email)"
 *    - Who has access: "Anyone"
 * 5. Copy the deployed Web App URL and add it to your App's build configuration.
 */

// Central Configuration
var SPREADSHEET_ID = SpreadsheetApp.getActiveSpreadsheet().getId();

function doGet(e) {
  try {
    var action = e.parameter.action;
    if (!action) {
      return jsonResponse({ success: false, message: "Missing action parameter" }, 400);
    }

    var sheet = SpreadsheetApp.openById(SPREADSHEET_ID);
    
    switch (action) {
      case "getDirectoryItems":
        return getTableData(sheet, "DirectoryItems"); // Or individual sheets if preferred
      case "getRoads":
        return getTableData(sheet, "Roads");
      case "getRoadTracks":
        return getTableData(sheet, "RoadTracks");
      case "getWorkers":
        return getTableData(sheet, "Workers");
      case "getJobs":
        return getTableData(sheet, "Jobs");
      case "getHospitals":
        return getTableData(sheet, "Hospitals");
      case "getDoctors":
        return getTableData(sheet, "Doctors");
      case "getEducation":
        return getTableData(sheet, "Education");
      case "getBusinesses":
        return getTableData(sheet, "Businesses");
      case "getTourism":
        return getTableData(sheet, "Tourism");
      case "getHistory":
        return getTableData(sheet, "History");
      case "getGovOffices":
        return getTableData(sheet, "GovOffices");
      case "getEmergency":
        return getTableData(sheet, "Emergency");
      case "getBloodDonors":
        return getTableData(sheet, "BloodDonors");
      case "getCitizenReports":
        return getTableData(sheet, "CitizenReports");
      case "getWeather":
        return getTableData(sheet, "Weather");
      case "getAgriculture":
        return getTableData(sheet, "Agriculture");
      case "getCrops":
        return getTableData(sheet, "Crops");
      case "getMarketPrices":
        return getTableData(sheet, "MarketPrice");
      case "getBusSchedules":
        return getTableData(sheet, "BusSchedules");
      case "getLaunchSchedules":
        return getTableData(sheet, "LaunchSchedules");
      case "getDonations":
        return getTableData(sheet, "Donations");
      case "getNotifications":
        return getTableData(sheet, "Notifications");
      case "getDeveloper":
        return getTableData(sheet, "Developer");
      case "getSettings":
        return getTableData(sheet, "Settings");
      case "getAnalytics":
        return getTableData(sheet, "Analytics");
      case "getDistricts":
        return getTableData(sheet, "Districts");
      case "getUpazilas":
        return getTableData(sheet, "Upazilas");
      case "getUnions":
        return getTableData(sheet, "Unions");
      default:
        return jsonResponse({ success: false, message: "Unknown action: " + action }, 400);
    }
  } catch (error) {
    return jsonResponse({ success: false, message: error.toString() }, 500);
  }
}

function doPost(e) {
  try {
    var requestBody = e.postData.contents;
    var data = JSON.parse(requestBody);
    var action = data.action;

    if (!action) {
      return jsonResponse({ success: false, message: "Missing action in POST body" }, 400);
    }

    var sheet = SpreadsheetApp.openById(SPREADSHEET_ID);
    logActivity("POST", action, JSON.stringify(data));

    switch (action) {
      case "registerUser":
        return registerUser(sheet, data);
      case "loginUser":
        return loginUser(sheet, data);
      case "addDirectoryItem":
        return addDirectoryItem(sheet, data);
      case "addSmartRoad":
        return addSmartRoad(sheet, data);
      case "addBloodDonor":
        return addBloodDonor(sheet, data);
      case "addCitizenReport":
        return addCitizenReport(sheet, data);
      case "submitReview":
        return submitReview(sheet, data);
      case "submitDonation":
        return submitDonation(sheet, data);
        
      // Admin Approvals Engine Actions
      case "adminApproveReject":
        return handleApproval(sheet, data);
      case "adminUpdateSettings":
        return updateSettings(sheet, data);
      case "adminBroadcastNotification":
        return broadcastNotification(sheet, data);
        
      default:
        return jsonResponse({ success: false, message: "Unknown POST action: " + action }, 400);
    }
  } catch (error) {
    return jsonResponse({ success: false, message: error.toString() }, 500);
  }
}

// Helper: Fetch sheet data as JSON array
function getTableData(sheet, tabName) {
  var tab = sheet.getSheetByName(tabName);
  if (!tab) {
    return jsonResponse({ success: false, message: "Sheet tab '" + tabName + "' not found." }, 404);
  }
  
  var values = tab.getDataRange().getValues();
  if (values.length <= 1) {
    return jsonResponse({ success: true, data: [] });
  }

  var headers = values[0];
  var list = [];
  
  for (var r = 1; r < values.length; r++) {
    var row = values[r];
    var item = {};
    for (var c = 0; c < headers.length; c++) {
      item[headers[c]] = row[c];
    }
    list.push(item);
  }

  return jsonResponse({ success: true, data: list });
}

// User Registration Handler
function registerUser(sheet, data) {
  var tab = sheet.getSheetByName("Users");
  if (!tab) return jsonResponse({ success: false, message: "Users sheet not found" }, 500);

  var email = data.email;
  var phone = data.phone;
  var name = data.name;
  var password = data.password; // Note: In production, hash passwords before saving!
  var role = data.role || "USER"; // USER, ADMIN

  var values = tab.getDataRange().getValues();
  for (var i = 1; i < values.length; i++) {
    if (values[i][1] === email || values[i][2] === phone) {
      return jsonResponse({ success: false, message: "User already registered with this email or phone" }, 400);
    }
  }

  var userId = "user_" + new Date().getTime();
  tab.appendRow([userId, email, phone, name, password, role, new Date().toISOString()]);
  
  return jsonResponse({ success: true, userId: userId, role: role, message: "Registration successful" });
}

// User Login Handler
function loginUser(sheet, data) {
  var tab = sheet.getSheetByName("Users");
  if (!tab) return jsonResponse({ success: false, message: "Users sheet not found" }, 500);

  var username = data.username; // Email or Phone
  var password = data.password;

  var values = tab.getDataRange().getValues();
  for (var i = 1; i < values.length; i++) {
    var row = values[i];
    if ((row[1] === username || row[2] === username) && row[4] === password) {
      return jsonResponse({
        success: true,
        user: {
          userId: row[0],
          email: row[1],
          phone: row[2],
          name: row[3],
          role: row[5]
        },
        token: "token_" + Utilities.getUuid()
      });
    }
  }

  return jsonResponse({ success: false, message: "Invalid email/phone or password" }, 401);
}

// Insert general dynamic Directory Item
function addDirectoryItem(sheet, data) {
  var tab = sheet.getSheetByName("DirectoryItems");
  if (!tab) {
    // If not exists, fall back or auto create
    tab = sheet.getSheetByName("Businesses") || sheet.getSheetByName("Hospitals");
  }
  if (!tab) return jsonResponse({ success: false, message: "Database storage not set up" }, 500);

  var item = data.item;
  var id = item.id || "item_" + new Date().getTime();
  var category = item.category || "business";
  var title = item.title || "";
  var subtitle = item.subtitle || "";
  var description = item.description || "";
  var location = item.location || "";
  var contactPhone = item.contactPhone || "";
  var rating = item.rating || 5.0;
  var status = "PENDING"; // Stays pending until Admin Approval
  
  tab.appendRow([id, category, title, subtitle, description, location, contactPhone, rating, status, new Date().toISOString()]);
  return jsonResponse({ success: true, itemId: id, status: status, message: "Submitted successfully. Awaiting approval." });
}

// Insert Smart Road
function addSmartRoad(sheet, data) {
  var tab = sheet.getSheetByName("Roads");
  if (!tab) return jsonResponse({ success: false, message: "Roads sheet not found" }, 500);

  var road = data.road;
  var id = road.id || "road_" + new Date().getTime();
  var name = road.name || "";
  var category = road.category || "";
  var width = road.width || "";
  var condition = road.condition || "";
  var description = road.description || "";
  var startPoint = road.startPoint || "";
  var endPoint = road.endPoint || "";
  var coordinatesJson = road.coordinatesJson || "[]";
  var district = road.district || "";
  var upazila = road.upazila || "";
  var unionName = road.unionName || "";
  var status = "PENDING"; // Must be approved by admin
  var contributor = road.contributor || "Contributor";
  var approvedDate = "";
  var lastUpdated = new Date().toISOString();
  var distance = road.distance || 0.0;
  var durationSeconds = road.durationSeconds || 0;

  tab.appendRow([
    id, name, category, width, condition, description, startPoint, endPoint,
    coordinatesJson, district, upazila, unionName, status, contributor, approvedDate, lastUpdated, distance, durationSeconds
  ]);

  return jsonResponse({ success: true, roadId: id, status: status, message: "Smart Road submitted. Awaiting admin review." });
}

// Add Blood Donor
function addBloodDonor(sheet, data) {
  var tab = sheet.getSheetByName("BloodDonors");
  if (!tab) return jsonResponse({ success: false, message: "BloodDonors sheet not found" }, 500);

  var donor = data.donor;
  var id = "donor_" + new Date().getTime();
  tab.appendRow([
    id, donor.name, donor.bloodGroup, donor.location, donor.contactPhone,
    donor.bio, "PENDING", new Date().toISOString()
  ]);

  return jsonResponse({ success: true, donorId: id, message: "Blood Donor registration submitted for admin approval" });
}

// Add Citizen Report
function addCitizenReport(sheet, data) {
  var tab = sheet.getSheetByName("CitizenReports");
  if (!tab) return jsonResponse({ success: false, message: "CitizenReports sheet not found" }, 500);

  var r = data.report;
  var id = "report_" + new Date().getTime();
  tab.appendRow([
    id, r.title, r.description, r.category, r.location, r.phone,
    r.status || "PENDING", r.upvoteCount || 0, r.reportedDate || new Date().toISOString()
  ]);

  return jsonResponse({ success: true, reportId: id, message: "Citizen report recorded successfully." });
}

// Submit a donation campaign entry
function submitDonation(sheet, data) {
  var tab = sheet.getSheetByName("Donations");
  if (!tab) return jsonResponse({ success: false, message: "Donations sheet not found" }, 500);

  var donation = data.donation;
  var id = "don_" + new Date().getTime();
  tab.appendRow([
    id, donation.contributor, donation.amount, donation.paymentMethod,
    donation.transactionId, "PENDING", new Date().toISOString()
  ]);

  return jsonResponse({ success: true, donationId: id, message: "Donation receipt submitted. Thank you for your support!" });
}

// Submit Review ratings
function submitReview(sheet, data) {
  var tab = sheet.getSheetByName("Reviews");
  if (!tab) return jsonResponse({ success: false, message: "Reviews sheet not found" }, 500);

  var id = "rev_" + new Date().getTime();
  tab.appendRow([
    id, data.itemId, data.user, data.rating, data.comment, new Date().toISOString()
  ]);

  return jsonResponse({ success: true, reviewId: id });
}

// Central Admin Approval, Edit, Delete Engine
function handleApproval(sheet, data) {
  var targetTabName = data.targetTab; // e.g. "DirectoryItems", "Roads", "BloodDonors", "CitizenReports", "Donations"
  var id = data.id;
  var statusAction = data.statusAction; // APPROVED, REJECTED, REMOVED
  var reason = data.reason || "";

  var tab = sheet.getSheetByName(targetTabName);
  if (!tab) return jsonResponse({ success: false, message: "Target sheet '" + targetTabName + "' not found" }, 404);

  var values = tab.getDataRange().getValues();
  var idColIndex = 0; // Usually first column is id
  var statusColIndex = -1;

  // Dynamically find 'status' column header
  var headers = values[0];
  for (var c = 0; c < headers.length; c++) {
    if (headers[c].toLowerCase() === "status") {
      statusColIndex = c;
      break;
    }
  }

  if (statusColIndex === -1) {
    return jsonResponse({ success: false, message: "Status column not found in sheet" }, 500);
  }

  for (var i = 1; i < values.length; i++) {
    if (values[i][idColIndex] == id) {
      var rowNum = i + 1;
      
      if (statusAction === "REMOVED") {
        tab.deleteRow(rowNum);
        return jsonResponse({ success: true, message: "Content item deleted permanently." });
      } else {
        tab.getCell(rowNum, statusColIndex + 1).setValue(statusAction);
        
        // Log rejection reason in Logs if provided
        if (statusAction === "REJECTED" && reason) {
          logActivity("REJECTION", id, "Reason: " + reason);
        }
        
        // Append dynamic notification if "Notifications" sheet exists
        var notifTab = sheet.getSheetByName("Notifications");
        if (notifTab) {
          notifTab.appendRow([
            "notif_" + new Date().getTime(),
            "Approval System",
            "আপনার দাখিলকৃত কনটেন্ট (" + id + ") " + (statusAction === "APPROVED" ? "অনুমোদিত হয়েছে!" : "বাতিল করা হয়েছে। কারণ: " + reason),
            "approval",
            new Date().toISOString()
          ]);
        }

        return jsonResponse({ success: true, message: "Item status changed to " + statusAction });
      }
    }
  }

  return jsonResponse({ success: false, message: "Item ID not found in database." }, 404);
}

// Update Global Admin Settings
function updateSettings(sheet, data) {
  var tab = sheet.getSheetByName("Settings");
  if (!tab) return jsonResponse({ success: false, message: "Settings sheet not found" }, 500);

  // Re-write or update keys
  var settings = data.settings; // JSON Object
  tab.clearContents();
  tab.appendRow(["Key", "Value"]);
  
  for (var key in settings) {
    tab.appendRow([key, settings[key]]);
  }

  return jsonResponse({ success: true, message: "Global Admin Settings updated successfully" });
}

// Broadcast alert to Notifications Tab
function broadcastNotification(sheet, data) {
  var tab = sheet.getSheetByName("Notifications");
  if (!tab) return jsonResponse({ success: false, message: "Notifications sheet not found" }, 500);

  var id = "broadcast_" + new Date().getTime();
  var type = data.type || "emergency"; // emergency, weather, cyclone, job, road, alert
  tab.appendRow([
    id, data.title, data.message, type, new Date().toISOString()
  ]);

  return jsonResponse({ success: true, message: "Broadcast alert published successfully to notifications hub" });
}

// Activity Logging
function logActivity(method, action, details) {
  try {
    var sheet = SpreadsheetApp.openById(SPREADSHEET_ID);
    var tab = sheet.getSheetByName("Logs");
    if (tab) {
      tab.appendRow([new Date().toISOString(), method, action, details]);
    }
  } catch (e) {
    // Ignore logging failures to avoid blocking APIs
  }
}

// Helper: Custom JSON Response Generator with CORS enabled
function jsonResponse(obj, statusCode) {
  var output = ContentService.createTextOutput(JSON.stringify(obj))
    .setMimeType(ContentService.MimeType.JSON);
  return output;
}
