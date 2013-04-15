#include "sync.h"

Mutex::Mutex()
{
	mutex_ = PTHREAD_MUTEX_INITIALIZER;
	pthread_mutexattr_t mta;

	pthread_mutexattr_init(&mta);
	pthread_mutexattr_settype(&mta, PTHREAD_MUTEX_RECURSIVE);

	pthread_mutex_init(&mutex_, &mta);
}

Mutex::~Mutex()
{
	pthread_mutex_destroy(&mutex_);
}

void Mutex::lock()
{
	pthread_mutex_lock(&mutex_);
}

void Mutex::unlock()
{
	pthread_mutex_unlock(&mutex_);
}