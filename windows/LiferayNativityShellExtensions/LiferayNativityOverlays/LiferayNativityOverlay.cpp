/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

#include "LiferayNativityOverlay.h"
#include "RegistryUtil.h"
#include "UtilConstants.h"
#include "ParserUtil.h"


#include <iostream>
#include <fstream>

using namespace std;

#pragma comment(lib, "shlwapi.lib")

extern HINSTANCE instanceHandle;

#define IDM_DISPLAY 0  
#define IDB_OK 101

LiferayNativityOverlay::LiferayNativityOverlay(): _communicationSocket(0), _referenceCount(1)
{
}

LiferayNativityOverlay::~LiferayNativityOverlay(void)
{
}

IFACEMETHODIMP_(ULONG) LiferayNativityOverlay::AddRef()
{
    return InterlockedIncrement(&_referenceCount);
}

IFACEMETHODIMP LiferayNativityOverlay::QueryInterface(REFIID riid, void **ppv)
{
    HRESULT hr = S_OK;

    if (IsEqualIID(IID_IUnknown, riid) ||  IsEqualIID(IID_IShellIconOverlayIdentifier, riid))
    {
        *ppv = static_cast<IShellIconOverlayIdentifier *>(this);
    }
    else
    {
        hr = E_NOINTERFACE;
        *ppv = NULL;
    }

    if (*ppv)
    {
        AddRef();
    }
	
    return hr;
}

IFACEMETHODIMP_(ULONG) LiferayNativityOverlay::Release()
{
    ULONG cRef = InterlockedDecrement(&_referenceCount);
    if (0 == cRef)
    {
        delete this;
    }

    return cRef;
}

IFACEMETHODIMP LiferayNativityOverlay::GetPriority(int *pPriority)
{
	pPriority = 0;

	return S_OK;
}

 IFACEMETHODIMP LiferayNativityOverlay::IsMemberOf(PCWSTR pwszPath, DWORD dwAttrib)
{
	
	if(!_IsOverlaysEnabled())
	{
		return MAKE_HRESULT(S_FALSE, 0, 0);
	}

	if(!_IsMonitoredFile(pwszPath))
	{
		return MAKE_HRESULT(S_FALSE, 0, 0);
	}
	
	if(!_IsMonitoredFileState(pwszPath))
	{
		return MAKE_HRESULT(S_FALSE, 0, 0);
	}

	return MAKE_HRESULT(S_OK, 0, 0);
}

IFACEMETHODIMP LiferayNativityOverlay::GetOverlayInfo(PWSTR pwszIconFile, int cchMax, int *pIndex, DWORD *pdwFlags)
{
	*pIndex = 0;

	*pdwFlags = ISIOI_ICONFILE | ISIOI_ICONINDEX;

	if (GetModuleFileName(instanceHandle, pwszIconFile, cchMax) == 0)
	{	
		HRESULT hResult = HRESULT_FROM_WIN32(GetLastError());

		return hResult;
	}

	return S_OK;
}


bool LiferayNativityOverlay::_IsOverlaysEnabled()
{
	int* enable = new int();
	bool success = false;

	if(RegistryUtil::ReadRegistry(REGISTRY_ROOT_KEY, REGISTRY_ENABLE_OVERLAY, enable))
	{
		if(enable)
		{
			success = true;
		}
	}

	delete enable;
	return success;
}

bool LiferayNativityOverlay::_IsMonitoredFile(const wchar_t* filePath)
{
	wstring* rootFolder = new wstring();
	bool needed = false;
	
	if(RegistryUtil::ReadRegistry(REGISTRY_ROOT_KEY, REGISTRY_FILTER_PATH, rootFolder))
	{
		if(FileUtil::IsChildFile(rootFolder->c_str(), filePath))
		{
			needed = true;
		}
	}

	delete rootFolder;
	return needed;
}

bool LiferayNativityOverlay::_IsMonitoredFileState(const wchar_t* filePath)
{
	bool needed = false;

	if(_communicationSocket == 0)
	{
		_communicationSocket = new CommunicationSocket(PORT);
	}

	wstring* message = new wstring();

	NativityMessage* nativityMessage = new NativityMessage();
	nativityMessage->SetCommand(new wstring(GET_FILE_OVERLAY_ID));
	nativityMessage->SetValue(new wstring(filePath));

	if(!ParserUtil::SerializeMessage(nativityMessage, message))
	{
		delete message;
		delete nativityMessage;

		return false;
	}

	wstring* response = new wstring();

	if(!_communicationSocket->SendMessageReceiveResponse(message->c_str(), response))
	{
		delete message;
		delete response;

		return false;
	}

	wstring* valueString = new wstring();

	if(!ParserUtil::GetItem(VALUE, response, valueString))
	{
		delete message;
		delete response;
		delete valueString;

		return false;
	}

	if(valueString->size() == 0)
	{
		delete message;
		delete response;
		delete valueString;

		return false;
	}

	int state = _wtoi(valueString->c_str());

	if(state == OVERLAY_ID)
	{
		needed = true;
	}

	delete message;
	delete response;
	delete valueString;

	return needed;
}
