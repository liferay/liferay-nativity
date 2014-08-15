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

#ifndef LIFERAYNATIVITYOVERLAY_H
#define LIFERAYNATIVITYOVERLAY_H

#include "RegistryUtil.h"
#include "StringUtil.h"
#include "UtilConstants.h"
#include "json/json.h"
#include "stdafx.h"

#include <iostream>
#include <fstream>

#pragma once

class LiferayNativityOverlay : public IShellIconOverlayIdentifier

{
	public:
		LiferayNativityOverlay();

		IFACEMETHODIMP_(ULONG) AddRef();

		IFACEMETHODIMP GetOverlayInfo(PWSTR pwszIconFile, int cchMax, int* pIndex, DWORD* pdwFlags);

		IFACEMETHODIMP GetPriority(int* pPriority);

		IFACEMETHODIMP IsMemberOf(PCWSTR pwszPath, DWORD dwAttrib);

		IFACEMETHODIMP QueryInterface(REFIID riid, void** ppv);

		IFACEMETHODIMP_(ULONG) Release();

	protected:
		~LiferayNativityOverlay(void);

	private:
		bool _IsOverlaysEnabled();

		bool _IsMonitoredFileState(const wchar_t* filePath);

		long _referenceCount;

		CommunicationSocket* _communicationSocket;
};

#endif