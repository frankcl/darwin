function buildLink(url, category, headers, userDefinedMap) {
    if (category === undefined || category === null || category < 0 || category > 3) {
        throw '链接类型不符合预期[' + category + ']';
    }
    var link = {};
    link.url = url;
    link.category = category;
    if (typeof headers != 'undefined' && headers !== null) link.headers = headers;
    if (typeof userDefinedMap != 'undefined' && userDefinedMap !== null) link.userDefinedMap = userDefinedMap;
    return link;
}

function buildErrorResponse(message) {
    var response = {};
    response.status = false;
    response.message = message;
    return response;
}

function buildFollowLinkResponse(followLinks) {
    var response = {};
    response.status = true;
    response.followLinks = followLinks;
    return response;
}

function buildStructureResponse(structureMap, userDefinedMap) {
    var response = {};
    response.status = true;
    response.structureMap = structureMap;
    response.userDefinedMap = userDefinedMap;
    return response;
}