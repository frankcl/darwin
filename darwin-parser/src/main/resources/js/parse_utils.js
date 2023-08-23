function buildURLRecord(url, category, headers, userDefinedMap) {
    if (category === undefined || category === null || category < 0 || category > 3) {
        throw '链接类型不符合预期[' + category + ']';
    }
    var urlRecord = {};
    urlRecord.url = url;
    urlRecord.category = category;
    if (typeof headers != 'undefined' && headers !== null) urlRecord.headers = headers;
    if (typeof userDefinedMap != 'undefined' && userDefinedMap !== null) urlRecord.userDefinedMap = userDefinedMap;
    return urlRecord;
}

function buildErrorResponse(message) {
    var response = {};
    response.status = false;
    response.message = message;
    return response;
}

function buildFollowURLsResponse(followURLs) {
    var response = {};
    response.status = true;
    response.followURLs = followURLs;
    return response;
}

function buildStructureResponse(structureMap, userDefinedMap) {
    var response = {};
    response.status = true;
    response.structureMap = structureMap;
    response.userDefinedMap = userDefinedMap;
    return response;
}