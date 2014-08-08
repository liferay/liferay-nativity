#ifndef __SYNC_H__
#define __SYNC_H__

#include <pthread.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <string>

class Mutex
{
	public:
		Mutex();
		~Mutex();

		void lock();
		void unlock();
	private:
		pthread_mutex_t mutex_;
};

class Guard
{
	public:
		Guard(Mutex& mutex) :
			mutex_(mutex)
		{
			mutex_.lock();
		}
		~Guard()
		{
			mutex_.unlock();
		}
	private:
		Mutex& mutex_;
};

#endif