function buildLinkURL(url, headers, userDefinedMap) {
    var linkURL = {};
    linkURL.url = url;
    if (typeof headers != 'undefined' && headers !== null) linkURL.headers = headers;
    if (typeof userDefinedMap != 'undefined' && userDefinedMap !== null) linkURL.userDefinedMap = userDefinedMap;
    return linkURL;
}

function buildErrorResponse(message) {
    var response = {};
    response.status = false;
    response.message = message;
    return response;
}

function buildFollowLinkResponse(followLinks, userDefinedMap) {
    var response = {};
    response.status = true;
    response.followLinks = followLinks;
    response.userDefinedMap = userDefinedMap;
    return response;
}

function buildStructureResponse(structureMap, userDefinedMap) {
    var response = {};
    response.status = true;
    response.structureMap = structureMap;
    response.userDefinedMap = userDefinedMap;
    return response;
}