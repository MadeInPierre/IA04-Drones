import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import math

import glob

# filename = input("File name to be opened (default=2d_signals.txt):")
# print(filename)
# filename = "logs/2d_signals.txt" if not filename else "logs/" + filename + ".txt"

class Link:
    def __init__(self):
        self.times            = []
        self.pos_x            = []
        self.pos_y            = []
        self.signals_raw      = []
        self.signals_filtered = []
        self.signals_true     = []


class Set:
    def __init__(self, name):
        self.name                  = name
        self.simulations           = []
        self.convergence_times     = []
        self.oscillation_variances = []


def read_file(filename):
    links = {}
    f = open(filename, "r")
    for line in f:
        try:
            weightid, step, from_id, to_id, from_pos, to_pos, signal_raw, signal_filtered, signal_true = line[:-1].split(',')
            if float(signal_true) > 0:
                if not int(weightid) in links.keys():
                    links[int(weightid)] = Link()

                links[int(weightid)].times.append(float(step))
                p1, p2 = float(from_pos),float(to_pos)
                links[int(weightid)].pos_x.append(min(p1, p2))
                links[int(weightid)].pos_y.append(max(p1, p2))
                links[int(weightid)].signals_raw.append(float(signal_raw))
                links[int(weightid)].signals_filtered.append(float(signal_filtered))
                links[int(weightid)].signals_true.append(float(signal_true))

        except:
            print("Ignored " + line[:-1])
            break
    f.close()
    return links

'''
def plot_pair_drones():
    plt.scatter(pos_x, pos_y, c=signals, cmap='plasma')

    r = range(int(max(pos_x + pos_y)))
    plt.plot(r, r, c='k')
    
    plt.title('Position based signal logging')
    plt.xlabel('p1')
    plt.ylabel('p2')
    plt.axis('equal')
    plt.colorbar()

def plot_distance_signal():
    plt.scatter(pos_y, signals, s=1)
    
    plt.title('Position based signal logging')
    plt.xlabel('Drone distance')
    plt.ylabel('Quality')
    plt.ylim(bottom=0, top=100)

def plot_time_filter():
    link_nb = 2
    plt.plot(links.get(link_nb).steps, links.get(link_nb).signals, 
            c='k',
            linewidth=1,
            label="Raw signal")

    plt.plot(links.get(link_nb+1).steps, links.get(link_nb+1).signals_filtered, 
            c='r',
            linewidth=3,
            label="Kalman filtered")

    # plt.title('Time based signal logging')
    plt.xlabel('Simulation step')
    plt.ylabel('Simulated RSSI')
    # plt.legend(loc='upper left', fontsize='small', ncol=5)
    plt.legend(ncol=2, fontsize='small', loc='upper left')
    plt.ylim(bottom=30, top=75)
    plt.show()
'''

def plot_time_signal(links):
    colors = ['tab:blue', 'tab:orange', 'tab:green', 'tab:red', 'tab:purple'][::-1]
    labels= []
        
    for i, k in enumerate(reversed(list(links.keys()))):
        plt.plot(links.get(k).times, links.get(k).signals_raw, 
            c=colors[i+1],
            linewidth=3,
            alpha=0.25,)

        plt.plot(links.get(k).times, links.get(k).signals_filtered, 
                c=colors[i+1],
                linewidth=3,
                label="Link {}".format(str(k)))
        
        plt.plot(links.get(k).times, links.get(k).signals_true, 
            c='k',
            linewidth=3,
            linestyle='--')

        labels.append("Link {}".format(str(k)))

    # plt.title('Time based signal logging')
    plt.xlabel('Simulation time (s)')
    plt.ylabel('Simulated RSSI')
    # plt.legend(loc='upper left', fontsize='small', ncol=5)
    plt.legend(reversed(plt.legend().legendHandles), reversed(labels), ncol=5, fontsize='small', loc='upper left')
    # plt.ylim(bottom=30, top=75)
    plt.show()


def plot_time_dist(links):
    colors = ['tab:blue', 'tab:orange', 'tab:green', 'tab:red', 'tab:purple'][::-1]
    labels= []
    for i, k in enumerate(reversed(list(links.keys()))):
        plt.plot(links.get(k).times, links.get(k).pos_y, 
                c=colors[i],
                linewidth=3,
                label="Link {}".format(str(k)))
        labels.append("Link {}".format(str(k)))

    # plt.title('Time based signal logging')
    plt.xlabel('Simulation time (s)')
    plt.ylabel('Tunnel distance (m)')
    # plt.legend(loc='upper left', fontsize='small', ncol=5)
    plt.legend(reversed(plt.legend().legendHandles), reversed(labels), ncol=5, fontsize='small', loc='upper left')
    plt.ylim(top=max(links.get(0).pos_y) + 10)
    plt.show()


def simulations_stats(simulations):
    print("\n\n------------\nPlotting convergence times...")
    convergence_times = []
    oscillation_variances = []
    n_simulations = 0

    for sim_i, sim in enumerate(simulations):
        print("Simulation {}".format(sim_i))
        convergence_time = -1
        convergence_step = -1
        success = True

        # Search for the convergence time
        n_steps = len(sim.get(list(sim.keys())[0]).times) - 1
        for step in range(0, n_steps):
            step_min = 200
            step_max = 0

            for k in sim.keys():
                if k is not 3:
                    link_signal = sim.get(k).signals_true[step]
                    step_min = link_signal if link_signal < step_min else step_min
                    step_max = link_signal if link_signal > step_max else step_max
                    if link_signal > 100:
                        success = False

            if convergence_step == -1 and step_max - step_min < 5:
                convergence_step = step
                convergence_time = sim.get(list(sim.keys())[0]).times[step]
        
        if not success:
            print("    FAILED")
            continue
        
        print("    Convergence time = {}".format(convergence_time))
        convergence_times.append(convergence_time)

        # Get the oscillations
        for k in sim.keys():
            link_positions = sim.get(k).pos_y[convergence_step:]
            link_variance = math.sqrt(np.var(link_positions))

            if k not in [0, 3]: # Ignore head and landed drones
                print("    Variance of link {} = {:.2f}".format(k, link_variance))
                oscillation_variances.append(link_variance)
        
        n_simulations += 1

    print("Found {}/{} successful simulations".format(n_simulations, len(simulations)))
    print("    Final convergences : {}".format(np.around(convergence_times,2)))
    print("    Final variances : {}".format(np.around(oscillation_variances,2)))
    return convergence_times, oscillation_variances


def plot_convergences(sets):
    times = [s.convergence_times for s in sets]
    names = [s.name for s in sets]
    plt.boxplot(times, labels=names)
    plt.show()


def plot_oscillations(sets):
    variances = [s.oscillation_variances for s in sets]
    names = [s.name for s in sets]
    plt.boxplot(variances, labels=names)
    plt.show()


if __name__ == "__main__":
    matplotlib.rcParams.update({'font.size': 34})
    matplotlib.rcParams['mathtext.fontset'] = 'stix'
    matplotlib.rcParams['font.family'] = 'STIXGeneral'

    # set_names = ["90_notolerance", "90_5tolerance", "90_kalman"]
    # set_names = ["conv_notolerance", "conv_5tolerance", "conv_kalman"]
    set_names = ["fixed_notolerance", "fixed_5tolerance", "fixed_kalman"]


    sets = []
    for set_name in set_names:
        s = Set(set_name)
        for f in glob.glob(set_name + "/2d_signals_*.txt"):
            s.simulations.append(read_file(f))
        s.convergence_times, s.oscillation_variances = simulations_stats(s.simulations)
        sets.append(s)

    for s in sets:
        print("Set '{}' converges at {:.2f} with oscillations {:.2f}".format(s.name, np.mean(s.convergence_times), np.mean(s.oscillation_variances)))

    plot_time_signal(read_file("logs/2d_signals_2.txt"))
    plot_time_dist(read_file("logs/2d_signals_2.txt"))

    plot_convergences(sets)
    plot_oscillations(sets)

