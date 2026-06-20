# Asset Cost Report APIs Verification Report
Generated at: 2026-06-20 14:08:22 (UTC)

### Status Summary: **12 / 12 Passed**

| Category | Method | Endpoint | Authorized Role | Status | Result |
| --- | --- | --- | --- | --- | --- |
| 1. Dashboard | `GET` | `/finance/asset-cost-report/dashboard` | `FINANCE` | `200` | ✅ PASSED |
| 2. Asset Cost Breakdown | `GET` | `/finance/asset-cost-report` | `FINANCE` | `200` | ✅ PASSED |
| 3. Category Details | `GET` | `/finance/asset-cost-report/categories/1` | `FINANCE` | `200` | ✅ PASSED |
| 4. Category Assets | `GET` | `/finance/asset-cost-report/categories/1/assets` | `FINANCE` | `200` | ✅ PASSED |
| 5. Asset Financial Details | `GET` | `/finance/asset-cost-report/assets/1` | `FINANCE` | `200` | ✅ PASSED |
| 6. Depreciation Report | `GET` | `/finance/asset-cost-report/depreciation` | `FINANCE` | `200` | ✅ PASSED |
| 7. Maintenance Cost Report | `GET` | `/finance/asset-cost-report/maintenance-cost` | `FINANCE` | `200` | ✅ PASSED |
| 8. Replacement Due Assets | `GET` | `/finance/asset-cost-report/replacement-due` | `FINANCE` | `200` | ✅ PASSED |
| 9. Export PDF | `GET` | `/finance/asset-cost-report/export/pdf` | `FINANCE` | `200` | ✅ PASSED |
| 10. Export CSV | `GET` | `/finance/asset-cost-report/export/csv` | `FINANCE` | `200` | ✅ PASSED |
| 11. Download PDF File | `GET` | `/api/v1/files/download/asset-cost-report-FY2026-27.pdf` | `FINANCE` | `200` | ✅ PASSED |
| 12. Download CSV File | `GET` | `/api/v1/files/download/asset-cost-report-FY2026-27.csv` | `FINANCE` | `200` | ✅ PASSED |

---

## Detailed Response Logs

### GET /finance/asset-cost-report/dashboard (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Asset cost dashboard retrieved successfully",
  "timestamp": "2026-06-20T08:38:22Z",
  "data": {
    "totalAssetValue": 892000.0,
    "annualDepreciation": 135200.0,
    "maintenanceCost": 52000.0,
    "replacementDue": 1,
    "assetCount": 7,
    "asOfDate": "2026-06-20"
  }
}
```

---

### GET /finance/asset-cost-report (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Asset cost breakdown retrieved successfully",
  "timestamp": "2026-06-20T08:38:22Z",
  "data": {
    "content": [
      {
        "categoryId": 1,
        "categoryName": "Laptop",
        "assetCount": 2,
        "totalValue": 200000.0,
        "annualDepreciation": 30000.0,
        "bookValue": 125000.0,
        "status": "ACTIVE"
      },
      {
        "categoryId": 2,
        "categoryName": "Mobile Device",
        "assetCount": 1,
        "totalValue": 110000.0,
        "annualDepreciation": 22000.0,
        "bookValue": 80000.0,
        "status": "ACTIVE"
      },
      {
        "categoryId": 3,
        "categoryName": "Display Monitor",
        "assetCount": 1,
        "totalValue": 45000.0,
        "annualDepreciation": 4500.0,
        "bookValue": 40000.0,
        "status": "ACTIVE"
      },
      {
        "categoryId": 4,
        "categoryName": "Accessories",
        "assetCount": 2,
        "totalValue": 37000.0,
        "annualDepreciation": 3700.0,
        "bookValue": 30000.0,
        "status": "ACTIVE"
      },
      {
        "categoryId": 5,
        "categoryName": "Servers",
        "assetCount": 1,
        "totalValue": 500000.0,
        "annualDepreciation": 75000.0,
        "bookValue": 350000.0,
        "status": "ACTIVE"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 5,
    "totalPages": 1
  }
}
```

---

### GET /finance/asset-cost-report/categories/1 (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Category cost details retrieved successfully",
  "timestamp": "2026-06-20T08:38:22Z",
  "data": {
    "categoryId": 1,
    "categoryName": "Laptop",
    "assetCount": 2,
    "totalValue": 200000.0,
    "annualDepreciation": 30000.0,
    "bookValue": 125000.0,
    "status": "ACTIVE",
    "averageAssetValue": 100000.0,
    "maintenanceCost": 13000.0
  }
}
```

---

### GET /finance/asset-cost-report/categories/1/assets (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Category assets retrieved successfully",
  "timestamp": "2026-06-20T08:38:22Z",
  "data": {
    "categoryId": 1,
    "categoryName": "Laptop",
    "assets": [
      {
        "assetId": 1,
        "assetTag": "SN-DL2024-421",
        "assetName": "Dell XPS 15",
        "purchaseValue": 120000.0,
        "currentValue": 115000.0,
        "assignedDate": "2025-12-20",
        "status": "ASSIGNED"
      },
      {
        "assetId": 4,
        "assetTag": "AST-1001",
        "assetName": "Dell Latitude 5400",
        "purchaseValue": 80000.0,
        "currentValue": 10000.0,
        "assignedDate": "2020-06-20",
        "status": "ASSIGNED"
      }
    ]
  }
}
```

---

### GET /finance/asset-cost-report/assets/1 (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Asset financial details retrieved successfully",
  "timestamp": "2026-06-20T08:38:22Z",
  "data": {
    "assetId": 1,
    "assetTag": "SN-DL2024-421",
    "assetName": "Dell XPS 15",
    "category": "LAPTOP",
    "brand": "Dell",
    "purchaseDate": "2025-12-20",
    "purchaseValue": 120000.0,
    "currentValue": 115000.0,
    "bookValue": 115000.0,
    "annualDepreciation": 18000.0,
    "depreciationRate": 15.0,
    "maintenanceCost": 5000.0,
    "warrantyExpiry": "2028-06-20",
    "replacementDue": false,
    "status": "ASSIGNED"
  }
}
```

---

### GET /finance/asset-cost-report/depreciation (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Depreciation report retrieved successfully",
  "timestamp": "2026-06-20T08:38:22Z",
  "data": {
    "financialYear": "FY2026-27",
    "totalDepreciation": 135200.0,
    "categories": [
      {
        "categoryName": "Laptop",
        "assetCount": 2,
        "depreciationAmount": 30000.0
      },
      {
        "categoryName": "Mobile Device",
        "assetCount": 1,
        "depreciationAmount": 22000.0
      },
      {
        "categoryName": "Display Monitor",
        "assetCount": 1,
        "depreciationAmount": 4500.0
      },
      {
        "categoryName": "Accessories",
        "assetCount": 2,
        "depreciationAmount": 3700.0
      },
      {
        "categoryName": "Servers",
        "assetCount": 1,
        "depreciationAmount": 75000.0
      }
    ]
  }
}
```

---

### GET /finance/asset-cost-report/maintenance-cost (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Maintenance cost report retrieved successfully",
  "timestamp": "2026-06-20T08:38:22Z",
  "data": {
    "totalMaintenanceCost": 52000.0,
    "assetsUnderMaintenance": 1,
    "categories": [
      {
        "categoryName": "Laptop",
        "maintenanceCost": 13000.0
      },
      {
        "categoryName": "Mobile Device",
        "maintenanceCost": 4000.0
      },
      {
        "categoryName": "Display Monitor",
        "maintenanceCost": 0
      },
      {
        "categoryName": "Accessories",
        "maintenanceCost": 0
      },
      {
        "categoryName": "Servers",
        "maintenanceCost": 35000.0
      }
    ]
  }
}
```

---

### GET /finance/asset-cost-report/replacement-due (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "Replacement due assets retrieved successfully",
  "timestamp": "2026-06-20T08:38:22Z",
  "data": {
    "count": 1,
    "assets": [
      {
        "assetId": 4,
        "assetTag": "AST-1001",
        "assetName": "Dell Latitude 5400",
        "category": "LAPTOP",
        "purchaseDate": "2020-06-20",
        "currentValue": 10000.0,
        "yearsInUse": 6,
        "replacementPriority": "HIGH"
      }
    ]
  }
}
```

---

### GET /finance/asset-cost-report/export/pdf (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "PDF export generated successfully",
  "timestamp": "2026-06-20T08:38:22Z",
  "data": {
    "fileName": "asset-cost-report-FY2026-27.pdf",
    "fileType": "application/pdf",
    "downloadUrl": "/api/v1/files/download/asset-cost-report-FY2026-27.pdf",
    "generatedAt": "2026-06-20T14:08:22.536506423"
  }
}
```

---

### GET /finance/asset-cost-report/export/csv (FINANCE)
**Status**: 200 | **Success**: True

```json
{
  "success": true,
  "message": "CSV export generated successfully",
  "timestamp": "2026-06-20T08:38:22Z",
  "data": {
    "fileName": "asset-cost-report-FY2026-27.csv",
    "fileType": "text/csv",
    "downloadUrl": "/api/v1/files/download/asset-cost-report-FY2026-27.csv",
    "generatedAt": "2026-06-20T14:08:22.550881211"
  }
}
```

---

### GET /api/v1/files/download/asset-cost-report-FY2026-27.pdf (FINANCE)
**Status**: 200 | **Success**: True

```json
File Content (1613 bytes)
```

---

### GET /api/v1/files/download/asset-cost-report-FY2026-27.csv (FINANCE)
**Status**: 200 | **Success**: True

```json
File Content (711 bytes)
```

---

