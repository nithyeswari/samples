import React, { useState } from 'react';
import { Search, ChevronDown, ChevronUp, ExternalLink, Upload } from 'lucide-react';
import * as XLSX from 'xlsx';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Alert,
  AlertDescription,
  AlertTitle,
} from "@/components/ui/alert";

const ApiCatalog = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [expandedApis, setExpandedApis] = useState({});
  const [apis, setApis] = useState([]);
  const [error, setError] = useState('');

  const processExcelData = (workbook) => {
    try {
      // Expect two sheets: "APIs" and "Endpoints"
      const apiSheet = workbook.Sheets["APIs"];
      const endpointSheet = workbook.Sheets["Endpoints"];

      if (!apiSheet || !endpointSheet) {
        throw new Error("Excel file must contain 'APIs' and 'Endpoints' sheets");
      }

      // Convert sheets to JSON
      const apisRaw = XLSX.utils.sheet_to_json(apiSheet);
      const endpointsRaw = XLSX.utils.sheet_to_json(endpointSheet);

      // Process and combine the data
      const processedApis = apisRaw.map(api => ({
        ...api,
        endpoints: endpointsRaw.filter(endpoint => endpoint.apiId === api.id)
          .map(endpoint => ({
            ...endpoint,
            parameters: endpoint.parameters ? JSON.parse(endpoint.parameters) : []
          }))
      }));

      setApis(processedApis);
      setError('');
    } catch (err) {
      setError('Error processing Excel file: ' + err.message);
    }
  };

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (file) {
      try {
        const buffer = await file.arrayBuffer();
        const workbook = XLSX.read(buffer, {
          cellStyles: true,
          cellFormulas: true,
          cellDates: true,
          cellNF: true,
          sheetStubs: true
        });
        processExcelData(workbook);
      } catch (err) {
        setError('Error reading Excel file: ' + err.message);
      }
    }
  };

  const toggleApiExpansion = (apiId) => {
    setExpandedApis(prev => ({
      ...prev,
      [apiId]: !prev[apiId]
    }));
  };

  const filteredApis = apis.filter(api =>
    api.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    api.description.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const getMethodColor = (method) => {
    const colors = {
      GET: 'bg-green-100 text-green-800',
      POST: 'bg-blue-100 text-blue-800',
      PUT: 'bg-yellow-100 text-yellow-800',
      DELETE: 'bg-red-100 text-red-800'
    };
    return colors[method] || 'bg-gray-100 text-gray-800';
  };

  return (
    <div className="container mx-auto p-4">
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-4">API Catalog</h1>
        
        {/* File Upload */}
        <div className="mb-4">
          <label className="block mb-2">
            <span className="sr-only">Choose Excel file</span>
            <div className="flex items-center gap-2 p-4 border-2 border-dashed rounded-lg cursor-pointer hover:bg-gray-50">
              <Upload size={20} className="text-gray-400" />
              <span className="text-gray-600">Upload Excel file</span>
              <input
                type="file"
                className="hidden"
                accept=".xlsx,.xls"
                onChange={handleFileUpload}
              />
            </div>
          </label>
        </div>

        {/* Error Alert */}
        {error && (
          <Alert variant="destructive" className="mb-4">
            <AlertTitle>Error</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {/* Search */}
        <div className="relative">
          <input
            type="text"
            placeholder="Search APIs..."
            className="w-full p-2 pl-10 border rounded-lg"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <Search className="absolute left-3 top-2.5 text-gray-400" size={20} />
        </div>
      </div>

      {/* API List */}
      <div className="space-y-4">
        {filteredApis.map(api => (
          <Card key={api.id} className="w-full">
            <CardHeader className="cursor-pointer" onClick={() => toggleApiExpansion(api.id)}>
              <div className="flex justify-between items-center">
                <div>
                  <CardTitle className="text-xl">{api.name}</CardTitle>
                  <CardDescription>{api.description}</CardDescription>
                </div>
                {expandedApis[api.id] ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
              </div>
            </CardHeader>

            {expandedApis[api.id] && (
              <CardContent>
                <div className="mb-4">
                  <div className="text-sm text-gray-600">Base URL:</div>
                  <div className="flex items-center">
                    <code className="bg-gray-100 p-1 rounded">{api.baseUrl}</code>
                    <ExternalLink className="ml-2 text-gray-400" size={16} />
                  </div>
                  <div className="text-sm text-gray-600 mt-2">Version: {api.version}</div>
                </div>

                <div className="space-y-4">
                  <h3 className="font-semibold">Endpoints</h3>
                  {api.endpoints.map((endpoint, index) => (
                    <div key={index} className="border rounded-lg p-4">
                      <div className="flex items-center gap-2 mb-2">
                        <span className={`px-2 py-1 rounded text-sm font-medium ${getMethodColor(endpoint.method)}`}>
                          {endpoint.method}
                        </span>
                        <code className="text-sm">{endpoint.path}</code>
                      </div>
                      <p className="text-gray-600 text-sm">{endpoint.description}</p>
                      
                      {endpoint.parameters && endpoint.parameters.length > 0 && (
                        <div className="mt-2">
                          <h4 className="text-sm font-medium mb-1">Parameters:</h4>
                          <div className="space-y-1">
                            {endpoint.parameters.map((param, pIndex) => (
                              <div key={pIndex} className="text-sm">
                                <code>{param.name}</code>
                                <span className="text-gray-500"> ({param.type})</span>
                                {param.required && <span className="text-red-500 ml-1">*</span>}
                              </div>
                            ))}
                          </div>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </CardContent>
            )}
          </Card>
        ))}
      </div>
    </div>
  );
};

export default ApiCatalog;