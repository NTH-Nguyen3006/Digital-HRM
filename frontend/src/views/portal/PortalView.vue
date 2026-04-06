<script setup>
import { useRouter } from 'vue-router'
import PortalLayout from '../../layouts/PortalLayout.vue'
import BaseButton from '../../components/common/BaseButton.vue'
import GlassCard from '../../components/common/GlassCard.vue'
import {
  ArrowRight,
  Users,
  Clock,
  ShieldCheck,
  FileText,
  HelpCircle,
  TrendingUp,
  MessageCircle,
  Zap,
  Layout,
  Star,
  Globe,
  LogIn
} from 'lucide-vue-next'

const router = useRouter()
''
const features = [
  {
    title: 'Đồng bộ Đa Chi nhánh',
    desc: 'Quản lý thông suốt nhân sự, chấm công, và chính sách trên toàn bộ mạng lưới công ty.',
    icon: Globe,
    color: 'bg-indigo-50 text-indigo-600'
  },
  {
    title: 'Dữ liệu Tập trung',
    desc: 'Hồ sơ nhân viên, hợp đồng và đánh giá năng lực được số hóa bảo mật và khoa học.',
    icon: ShieldCheck,
    color: 'bg-emerald-50 text-emerald-600'
  },
  {
    title: 'Tự phục vụ (ESS)',
    desc: 'Nhân viên chủ động thực hiện xin nghỉ, xem bảng lương và đề xuất nội bộ nhanh chóng.',
    icon: Users,
    color: 'bg-sky-50 text-sky-600'
  },
  {
    title: 'Báo cáo Thời gian thực',
    desc: 'Dashboard tổng quan về tình hình nhân sự của mọi chi nhánh mọi lúc.',
    icon: TrendingUp,
    color: 'bg-rose-50 text-rose-600'
  }
]

const services = [
  { id: 1, name: 'Đăng ký nghỉ phép', icon: Clock, path: '/leaves' },
  { id: 2, name: 'Tra cứu bảng lương', icon: FileText, path: '/payroll' },
  { id: 3, name: 'Hồ sơ cá nhân', icon: Users, path: '/portal/profile' },
  { id: 4, name: 'Lịch sử chấm công', icon: Zap, path: '/portal/attendance' },
  { id: 5, name: 'Sổ tay nhân sự', icon: HelpCircle, path: '#' },
  { id: 6, name: 'Kênh hỗ trợ HR', icon: MessageCircle, path: '#' },
]

const branches = [
  { id: 'hn', name: 'Trụ sở Hà Nội', address: 'Tòa nhà Landmark, Nam Từ Liêm', employees: 245, active: true },
  { id: 'hcm', name: 'Chi nhánh TP.HCM', address: 'Quận 1, Thành phố Hồ Chí Minh', employees: 180, active: true },
  { id: 'dn', name: 'Chi nhánh Đà Nẵng', address: 'Hải Châu, Thành phố Đà Nẵng', employees: 85, active: true },
]
</script>

<template>
  <PortalLayout>
    <div class="space-y-40 mb-40">
      <section
        class="max-w-7xl mx-auto px-6 pt-20 lg:pt-32 flex flex-col lg:flex-row items-center justify-between gap-16 relative">
        <div class="flex-1 space-y-10 animate-fade-in-up">
          <div
            class="inline-flex items-center px-4 py-2 rounded-2xl bg-indigo-50 text-indigo-700 text-sm font-bold border border-indigo-100/50">
            <Star class="w-4 h-4 mr-2" /> Hệ thống Quản trị Nội bộ Tập trung
          </div>
          <h1 class="text-6xl md:text-7xl font-black text-slate-900 leading-[1.1] tracking-tighter">
            Trải nghiệm Không gian <span
              class="text-transparent bg-clip-text bg-linear-to-r from-indigo-500 to-indigo-700">LÀM VIỆC SỐ</span>.
          </h1>
          <p class="text-xl text-slate-500 font-medium leading-relaxed max-w-xl">
            Kết nối toàn diện mọi chi nhánh. Cổng thông tin nhân sự nội bộ giúp tối ưu hoá quy trình, nâng cao trải
            nghiệm nhân viên và xây dựng môi trường làm việc chuyên nghiệp.
          </p>
          <div class="flex flex-col sm:flex-row items-center space-y-4 sm:space-y-0 sm:space-x-6">
            <BaseButton size="lg" :icon="LogIn" iconRight @click="router.push('/login')">
              Vào hệ thống quản trị
            </BaseButton>
            <div class="flex items-center space-x-4">
              <div class="flex -space-x-4">
                <div v-for="i in 3" :key="i"
                  class="w-12 h-12 rounded-full border-4 border-white bg-slate-100 flex items-center justify-center font-bold text-slate-400 text-sm">
                  C{{ i }}
                </div>
              </div>
              <p class="text-sm font-bold text-slate-400 leading-tight">Đang vận hành tại<br /><span
                  class="text-slate-700 font-black">{{ branches.length }} Chi nhánh</span></p>
            </div>
          </div>
        </div>

        <div class="flex-1 relative animate-scale-in">
          <!-- Abstract Graphic -->
          <div
            class="relative z-10 w-full aspect-square bg-linear-to-tr from-indigo-100 to-indigo-50 rounded-[4rem] overflow-hidden border-8 border-white shadow-2xl">
            <img src="https://placehold.co/400" alt="Work Culture"
              class="w-full h-full object-cover opacity-80 mix-blend-multiply" />
            <div class="absolute inset-0 bg-linear-to-t from-indigo-900/20 to-transparent"></div>
          </div>

          <!-- Floating elements -->
          <!-- <GlassCard padding="p-4" class="absolute -top-10 -right-10 w-48 shadow-2xl animate-float">
            <div class="flex items-center space-x-3">
              <div class="w-10 h-10 rounded-xl bg-emerald-500 flex items-center justify-center text-white">
                <Zap class="w-5 h-5" />
              </div>
              <div>
                <p class="text-xs font-bold text-slate-400">Hiệu năng</p>
                <p class="text-sm font-black text-slate-900">Tối ưu 40%</p>
              </div>
            </div>
          </GlassCard> -->

          <!-- <GlassCard padding="p-4" class="absolute -bottom-10 -left-10 w-56 shadow-2xl animate-float"
            style="animation-delay: -3s">
            <div class="flex items-center space-x-3">
              <div class="w-10 h-10 rounded-xl bg-indigo-600 flex items-center justify-center text-white">
                <Globe class="w-5 h-5" />
              </div>
              <div>
                <p class="text-xs font-bold text-slate-400">Kết nối</p>
                <p class="text-sm font-black text-slate-900">Toàn cầu hoá</p>
              </div>
            </div>
          </GlassCard> -->
        </div>

        <!-- Background Decorations -->
        <div class="absolute -top-40 -left-40 w-96 h-96 bg-indigo-50 rounded-full blur-[150px] -z-10 animate-pulse">
        </div>
      </section>

      <!-- Features Section -->
      <section id="features" class="max-w-7xl mx-auto px-6">
        <div class="text-center space-y-6 mb-24">
          <h2 class="text-5xl font-black text-slate-900 tracking-tight">Trải nghiệm khác biệt.</h2>
          <p class="text-lg text-slate-500 font-medium max-w-2xl mx-auto italic leading-relaxed">
            Chúng tôi mang đến giải pháp HRM tập trung vào hiệu suất và sự hài lòng của từng cá nhân trong tổ chức.
          </p>
        </div>

        <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
          <div v-for="f in features" :key="f.title"
            class="group p-8 rounded-[2.5rem] bg-white border border-slate-100 hover:border-indigo-100 hover:shadow-2xl hover:shadow-indigo-50 transition-all duration-500">
            <div
              :class="[f.color, 'w-14 h-14 rounded-2xl flex items-center justify-center mb-10 transition-transform group-hover:scale-110 group-hover:rotate-6']">
              <component :is="f.icon" class="w-7 h-7" />
            </div>
            <h3 class="text-xl font-bold text-slate-900 mb-4">{{ f.title }}</h3>
            <p class="text-slate-500 font-medium leading-relaxed">{{ f.desc }}</p>
          </div>
        </div>
      </section>

      <!-- Portal Services (Quick Links) -->
      <section id="services" class="bg-slate-950 py-40 px-6 relative overflow-hidden">
        <div class="absolute top-0 left-0 w-full h-full">
          <div class="absolute -top-24 -left-24 w-96 h-96 bg-indigo-600/10 rounded-full blur-[120px]"></div>
          <div class="absolute bottom-0 right-0 w-96 h-96 bg-emerald-500/10 rounded-full blur-[120px]"></div>
        </div>

        <div class="max-w-7xl mx-auto relative z-10">
          <div class="flex flex-col md:flex-row items-end justify-between gap-12 mb-24">
            <div class="space-y-6">
              <div class="text-emerald-400 font-bold uppercase tracking-widest text-sm">Self-Service Center</div>
              <h2 class="text-5xl font-black text-white tracking-tight">Dịch vụ cho nhân viên.</h2>
            </div>
            <p class="text-slate-400 font-medium text-lg max-w-md">Tiết kiệm thời gian với hệ thống tự phục vụ thông
              minh, minh bạch và tức thì.</p>
          </div>

          <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
            <div v-for="s in services" :key="s.id" @click="router.push(s.path)"
              class="p-8 rounded-4xl bg-white/5 border border-white/10 hover:bg-white/10 hover:border-white/20 transition-all duration-300 cursor-pointer group flex flex-col items-center text-center">
              <div
                class="w-16 h-16 rounded-2xl bg-indigo-600/20 text-indigo-400 flex items-center justify-center mb-6 group-hover:bg-indigo-600 group-hover:text-white transition-all scale-100 group-hover:scale-110">
                <component :is="s.icon" class="w-8 h-8" />
              </div>
              <span class="text-white font-bold text-sm leading-tight">{{ s.name }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- Branch Network Section -->
      <section id="network" class="max-w-7xl mx-auto px-6">
        <div class="flex flex-col md:flex-row items-center justify-between mb-24 gap-8 text-center md:text-left">
          <div class="space-y-4">
            <h2 class="text-5xl font-black text-slate-900 tracking-tight">Mạng lưới Chi nhánh.</h2>
            <p class="text-slate-500 font-medium max-w-xl">Hệ thống xử lý hàng nghìn dữ liệu cán bộ nhân viên đồng bộ
              theo thời gian thực tại các chi nhánh văn phòng trên toàn quốc.</p>
          </div>
          <div class="px-6 py-4 rounded-3xl bg-indigo-50 border border-indigo-100 flex items-center space-x-4">
            <div class="w-12 h-12 rounded-full bg-indigo-500 flex items-center justify-center animate-pulse">
              <Globe class="w-6 h-6 text-white" />
            </div>
            <div>
              <p class="text-sm font-bold text-indigo-900/60 leading-tight">Tổng nhân sự</p>
              <p class="text-2xl font-black text-indigo-700">510+</p>
            </div>
          </div>
        </div>

        <div class="grid lg:grid-cols-3 gap-8">
          <div v-for="branch in branches" :key="branch.id"
            class="group relative p-8 rounded-[2.5rem] bg-slate-50 border border-slate-200 hover:bg-white hover:border-indigo-200 hover:shadow-2xl hover:shadow-indigo-100/50 transition-all duration-500 overflow-hidden cursor-default">

            <div
              class="absolute top-0 right-0 p-8 sm:p-12 opacity-5 group-hover:opacity-10 transition-opacity transform group-hover:scale-110 duration-700 pointer-events-none">
              <Globe class="w-32 h-32" />
            </div>

            <div class="flex items-center space-x-4 mb-8">
              <div
                class="w-14 h-14 rounded-2xl bg-white shadow-sm flex items-center justify-center border border-slate-100">
                <div class="w-3 h-3 rounded-full bg-emerald-500 animate-pulse"></div>
              </div>
              <div>
                <div class="text-slate-400 font-bold text-xs uppercase tracking-widest mb-1">Office</div>
                <h3 class="text-xl font-black text-slate-900">{{ branch.name }}</h3>
              </div>
            </div>

            <div class="space-y-4 mb-8">
              <div class="flex items-center text-slate-600">
                <Layout class="w-5 h-5 mr-3 text-slate-400" />
                <span class="font-medium text-sm">{{ branch.address }}</span>
              </div>
              <div class="flex items-center text-slate-600">
                <Users class="w-5 h-5 mr-3 text-slate-400" />
                <span class="font-medium text-sm"><strong class="text-slate-900">{{ branch.employees }}</strong> nhân
                  sự</span>
              </div>
            </div>

            <div class="flex items-center text-indigo-600 font-bold text-sm">
              Trạng thái: Hoạt động ổn định
              <ShieldCheck class="w-4 h-4 ml-2 text-emerald-500" />
            </div>
          </div>
        </div>
      </section>
    </div>
  </PortalLayout>
</template>

<style scoped>
@reference "../../assets/css/main.css";

.animate-fade-in-up {
  animation: fadeInUp 1s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

.animate-scale-in {
  animation: scaleIn 1.2s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
