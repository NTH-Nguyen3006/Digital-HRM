<script setup>
import { ref } from 'vue'
import PortalLayout from '../../layouts/PortalLayout.vue'
import { User, Phone, Mail, MapPin, Briefcase } from 'lucide-vue-next'

const profile = ref({
  name: 'Nguyễn Văn Nhân Viên',
  id: 'NV999',
  department: 'Khối Kỹ thuật',
  role: 'Frontend Developer',
  phone: '0987654321',
  email: 'nhanvien@company.com',
  address: '123 Đường ABC, Quận XYZ, TP.HCM',
  status: 'Đang làm việc'
})
</script>

<template>
  <PortalLayout>
    <div class="max-w-4xl mx-auto pt-32 pb-20 px-6">
      <div class="bg-white rounded-3xl p-8 shadow-xl shadow-slate-200/50 border border-slate-100 relative overflow-hidden">
        <div class="absolute top-0 right-0 w-64 h-64 bg-indigo-50 rounded-bl-full -z-0"></div>
        
        <div class="relative z-10">
          <h1 class="text-3xl font-black text-slate-900 mb-8">Thông tin cá nhân</h1>
          
          <div class="flex flex-col md:flex-row gap-10 items-start">
            <div class="w-32 h-32 rounded-3xl bg-indigo-100 flex items-center justify-center text-indigo-600 shadow-md border-4 border-white shrink-0">
              <User class="w-16 h-16" />
            </div>
            
            <div class="flex-1 space-y-6">
              <div>
                <h2 class="text-2xl font-bold text-slate-900">{{ profile.name }}</h2>
                <span class="inline-block mt-2 px-3 py-1 bg-emerald-100 text-emerald-700 text-xs font-bold rounded-lg border border-emerald-200">{{ profile.status }}</span>
              </div>
              
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6 pt-6 border-t border-slate-100">
                <div class="space-y-1">
                  <div class="text-slate-400 text-xs font-bold uppercase tracking-widest flex items-center"><Briefcase class="w-3.5 h-3.5 mr-1" /> Mã NV & Chức vụ</div>
                  <div class="text-slate-900 font-bold text-lg">{{ profile.id }} - {{ profile.role }}</div>
                </div>
                
                <div class="space-y-1">
                  <div class="text-slate-400 text-xs font-bold uppercase tracking-widest flex items-center"><Briefcase class="w-3.5 h-3.5 mr-1" /> Phòng ban</div>
                  <div class="text-slate-700 font-medium text-lg">{{ profile.department }}</div>
                </div>
                
                <div class="space-y-1">
                  <div class="text-slate-400 text-xs font-bold uppercase tracking-widest flex items-center"><Phone class="w-3.5 h-3.5 mr-1" /> Điện thoại</div>
                  <div class="text-slate-700 font-medium text-lg">{{ profile.phone }}</div>
                </div>
                
                <div class="space-y-1">
                  <div class="text-slate-400 text-xs font-bold uppercase tracking-widest flex items-center"><Mail class="w-3.5 h-3.5 mr-1" /> Email</div>
                  <div class="text-slate-700 font-medium text-lg">{{ profile.email }}</div>
                </div>
                
                <div class="space-y-1 md:col-span-2">
                  <div class="text-slate-400 text-xs font-bold uppercase tracking-widest flex items-center"><MapPin class="w-3.5 h-3.5 mr-1" /> Địa chỉ</div>
                  <div class="text-slate-700 font-medium text-lg">{{ profile.address }}</div>
                </div>
              </div>
              
            </div>
          </div>
        </div>
      </div>
    </div>
  </PortalLayout>
</template>
